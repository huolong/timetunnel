package com.taobao.timetunnel2.client.dsl.disk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.taobao.timetunnel2.client.dsl.Message;
import com.taobao.timetunnel2.client.dsl.impl.Config;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-13
 * 
 */
public class FileChannelQueueImpl implements FileChannelQueue {
	private static Logger log = Logger.getLogger(FileChannelQueueImpl.class);
	private final FCQMeta meta;
	private final String queueName;

	private final DataFile dataFile;
	private FileChannel writeFileChannel;
	private final ByteBuffer writeLenBuffer = ByteBuffer.allocateDirect(4);

	private final ByteBuffer rawData = ByteBuffer.allocateDirect(Config.getInstance().getMaxLoadSize());;
	private final ByteBuffer lenBuffer = ByteBuffer.allocateDirect(4);
	private String readFile;
	private long readPos;
	boolean firstMessageInFile = false;
	private final ContentBuffer cb;
	private AtomicReference<MessageWapper> lastMessage;

	private final CountDownLatch latch = new CountDownLatch(1);
	private final LinkedBlockingQueue<MessageWapper> queue;
	private final ExecutorService readerTask;
	private AtomicBoolean stopped = new AtomicBoolean(false);
	private final ReentrantLock addLock = new ReentrantLock();

	public FileChannelQueueImpl(final String queueName) {
		this.meta = new FCQMeta(queueName);
		this.dataFile = new DataFile(queueName);
		this.cb = new ContentBuffer();
		loadMeta();
		log.debug("init meta: " + readFile + " and pos " + readPos);
		this.queueName = queueName;
		this.lastMessage = new AtomicReference<MessageWapper>(null);
		this.writeFileChannel = null;
		this.queue = new LinkedBlockingQueue<MessageWapper>(Config.getInstance().getReadQueueSize());
		this.readerTask = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "disk_queue_retriver_" + queueName);
			}
		});
		start();
	}

	private void loadMeta() {
		readPos = meta.getReadPos();
		readFile = meta.getFileName();
		if (readFile.equalsIgnoreCase("null")) {
			readFile = null;
		}
		String readFileFromQ = dataFile.getQueueFileName();
		if (readFileFromQ == null) {
			if (readFile != null) {
				log.error("meta file " + readFile + " not equal to " + null);
			}
			readFile = null;
			readPos = 0;
			return;
		}
		if (readFile == null) {
			readFile = readFileFromQ;
			readPos = 0;
		} else if (!readFile.equals(readFileFromQ)) {
			log.error("meta file " + readFile + " not equal to " + readFileFromQ);
			readFile = readFileFromQ;
			readPos = 0;
		}
		return;
	}

	private void start() {
		readerTask.submit(new Runnable() {
			@Override
			public void run() {
				try {
					loadMessages();
				} catch (Throwable t) {
					log.error("unhandle exception: ", t);
				}
			}
		});
	}

	public String getQueueName() {
		return queueName;
	}

	private void switchReadFile(FileChannel fc) {
		if (!dataFile.isEmpty()) {
			if (fc != null)
				try {
					fc.close();
				} catch (IOException ignore) {
					log.error("close file failed when switch file", ignore);
				}
			readFile = dataFile.getQueueFileName();
			readPos = 0;
		}
	}

	private void loadMessages() {
		if (readFile == null) {
			try {
				latch.await();
			} catch (InterruptedException e) {
				log.error("lacth interruptted", e);
				return;
			}
			switchReadFile(null);
			assert (readFile != null);
		}
		while (!stopped.get()) {
			FileChannel readChannel = openChannel();
			if (Thread.interrupted() || stopped.get())
				return;
			loadFromFile(readChannel);
			if (Thread.interrupted() || stopped.get())
				return;
			switchReadFile(readChannel);
		}

	}

	private void loadFromFile(FileChannel channel) {
		int readLen = 0;
		firstMessageInFile = readPos == 0 ? true : false;
		try {
			channel.position(readPos);
			while (readLen != -1 || dataFile.isEmpty()) {
				readLen = readFile(channel);
				while (rawData.hasRemaining()) {
					getAMessage();
					if (Thread.interrupted() || stopped.get())
						return;
				}
				delayLoad(readLen);
				if (Thread.interrupted() || stopped.get()) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			assert (readLen == -1 && !dataFile.isEmpty());
			readLen = 0;
			// do it again to close time window when this case happen:
			// read the last file to end, then last file grow up, and new file
			// create
			// then go to while condition, jump out loop, but the last appended
			// content
			// have not been read.
			while (readLen != -1) {
				readLen = readFile(channel);
				while (rawData.hasRemaining()) {
					getAMessage();
					if (Thread.interrupted() || stopped.get())
						return;
				}
			}

			checkIfHasIncompleteContent();

		} catch (IOException e) {
			try {
				channel.close();
			} catch (IOException e1) {
			}
			String errMessage = "fc io exception " + readFile;
			log.error(errMessage, e);
			throw new RuntimeException(errMessage, e);
		}
	}

	private void checkIfHasIncompleteContent() {
		if (lenBuffer.position() != 0 || cb.isActive()) {
			String errMsg = "wrong file content occurred in " + readFile + " at " + readPos;
			log.error(errMsg);
			throw new RuntimeException(errMsg);
		}
	}

	private int readFile(FileChannel channel) throws IOException {
		int readLen = 0;
		rawData.clear();
		readLen = channel.read(rawData);
		readPos = channel.position();
		rawData.flip();
		return readLen;
	}

	private void delayLoad(int readLen) {
		if (readLen == -1 && dataFile.isEmpty()) {
			delay(5);
		}
	}

	private void getAMessage() {
		int len = 0;
		if (cb.get() == null || !cb.isActive()) {
			if (lenBuffer.position() + rawData.remaining() < 4) {
				log.debug("incomplete len buffer " + lenBuffer.position() + " and " + rawData.remaining());
				lenBuffer.put(rawData);
				return;
			}
			int lr = lenBuffer.remaining();
			for (int i = 0; i < lr; i++)
				lenBuffer.put(rawData.get());
			lenBuffer.flip();
			len = lenBuffer.getInt();
			checkLen(len);
			lenBuffer.clear();
			cb.allocate(len);
		}
		if (cb.position() + rawData.remaining() < len) {
			log.debug("incomplete content buffer " + cb.position() + " and " + rawData.remaining());
			cb.put(rawData);
			return;
		}
		int cr = cb.remaining();
		for (int i = 0; i < cr; i++)
			cb.put(rawData.get());
		cb.flip();
		bulidMessage(cb);
		cb.clear();
		return;
	}

	private void bulidMessage(ContentBuffer contentBuffer) {
		MessageWapper messageWapper = new MessageWapper(contentBuffer, firstMessageInFile, readPos - rawData.remaining(), readFile);
		firstMessageInFile = false;
		try {
			queue.put(messageWapper);
		} catch (InterruptedException e) {
			log.error("put message failed", e);
			Thread.currentThread().interrupt();
		}
	}

	private void checkLen(int len) {
		if (len <= 0 || len > Integer.MAX_VALUE) {
			String errMsg = "wrong len in file " + readFile + " at " + readPos;
			log.error(errMsg);
			throw new RuntimeException(errMsg);
		}
	}

	private FileChannel openChannel() {
		FileChannel fc = null;
		Throwable t = null;
		for (int i = 0; i < 3; i++) {
			try {
				fc = new RandomAccessFile(readFile, "r").getChannel();
				return fc;
			} catch (FileNotFoundException e) {
				t = e;
				delay(i * 2);
			}
		}
		String errMsg = "open read file failed " + readFile;
		log.error(errMsg, t);
		throw new RuntimeException(errMsg, t);
	}

	private void delay(int i) {
		try {
			Thread.sleep(5 * (i + 1));
		} catch (InterruptedException ignore) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public ByteBuffer get() {
		MessageWapper take = null;
		confirmLastMessage();
		try {
			take = queue.take();
			lastMessage.set(take);
		} catch (InterruptedException e) {
			log.error("get interrupted when take messsage from FileChannelQueue", e);
		}
		return take == null ? null : take.getBf();
	}

	private void confirmLastMessage() {
		MessageWapper toConfirm = null;
		if ((toConfirm = lastMessage.getAndSet(null)) != null) {
			meta.update(toConfirm.getStartPos(), toConfirm.getCurrentFileName());
			if (toConfirm.isFirstMInFile()) {
				dataFile.deleteOlderFiles(toConfirm.getCurrentFileName());
			}
		}
	}

	@Override
	public void add(Message m) {
		addLock.lock();
		try {
			checkWriteChannel();
			write(m);
		} finally {
			addLock.unlock();
		}
		latch.countDown();
	}

	@Override
	public void close() {
		stopped.set(true);
		readerTask.shutdown();
		meta.close();
	}

	private void checkWriteChannel() {
		try {
			if (writeFileChannel == null) {
				writeFileChannel = new RandomAccessFile(dataFile.createQueueFile(), "rwd").getChannel();
			}
			if (writeFileChannel.size() > Config.getInstance().getMaxWriteSize()) {
				writeFileChannel.force(true);
				writeFileChannel.close();
				writeFileChannel = new RandomAccessFile(dataFile.createQueueFile(), "rwd").getChannel();
			}
		} catch (Exception e) {
			log.error("create write channel failed", e);
			throw new RuntimeException("create write channel failed", e);
		}
	}

	private void write(Message m) {
		try {
			byte[] c = m.serialize();
			writeLenBuffer.putInt(c.length);
			writeLenBuffer.flip();

			writeFileChannel.write(writeLenBuffer);
			writeLenBuffer.clear();
			writeFileChannel.write(ByteBuffer.wrap(c));
			writeFileChannel.force(true);
		} catch (Exception e) {
			log.error("write message failed", e);
			throw new RuntimeException("write message failed", e);
		}
	}
}
