package com.taobao.timetunnel.savefile.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.taobao.timetunnel.savefile.app.Conf;
import com.taobao.timetunnel.savefile.util.DateUtil;
import com.taobao.timetunnel.savefile.util.FileUtil;
import com.taobao.timetunnel.savefile.writer.FileWriter.OutputStreamStruct;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-20
 * 
 */
public class OutputStreamManager {
	private static OutputStreamManager instance = new OutputStreamManager(Conf.getInstance().getBaseDir());
	private final ReentrantReadWriteLock lock;

	public interface OutputStreamNameGenerator {
		public String generateFileName(String baseDir, String tag);
	}

	private static class DefaultOutputStreamNameGenerator implements OutputStreamNameGenerator {
		public String generateFileName(String baseDir, String tag) {
			return baseDir + "/" + tag + "/" + DateUtil.getTimeStampInMin('_', null) + "#" + UUID.randomUUID().toString() + ".tmp";
		}
	}

	public static OutputStreamManager getInstance() {
		return instance;
	}

	private Map<String, OutputStreamStruct> outputStreamMap;
	private String baseDir;
	private OutputStreamNameGenerator filenameGenerator = new DefaultOutputStreamNameGenerator();
	private static final long MAX_BYTES_PER_FILE = Long.MAX_VALUE;
	private static final Logger log = Logger.getLogger(OutputStreamManager.class);

	private void setFilenameGenerator(OutputStreamNameGenerator filenameGenerator) {
		this.filenameGenerator = filenameGenerator;
	}

	private OutputStreamManager(String baseDir) {
		this.lock = new ReentrantReadWriteLock();
		this.baseDir = baseDir;
		this.outputStreamMap = new ConcurrentHashMap<String, OutputStreamStruct>();
		setFileGenMode();
	}

	private void setFileGenMode() {
		if (Conf.getInstance().getfilePathCompatible())
			setFilenameGenerator(new OutputStreamNameGenerator() {
				public String generateFileName(String baseDir, String tag) {
					String ts = DateUtil.getTimeStampInMin('-', null);
					ts = ts.replaceFirst("-", "");
					ts = ts.replaceFirst("-", "");
					return baseDir + "/" + tag + "/" + tag + "-" + ts + ".tmp";
				}
			});
	}

	public void switchAllOutputStreams() throws IOException {
		lock.writeLock().lock();
		try {
			for (String tag : outputStreamMap.keySet()) {
				OutputStreamStruct outputStream = outputStreamMap.get(tag);
				if (outputStream != null && outputStream.stream != null) {
					if (outputStream.bytesWritten == 0) {
						IOUtils.write(new byte[1], outputStream.stream);
					}
					closeOutputStream(outputStream);
				}
				outputStream = newOutputStream(tag);
				outputStreamMap.put(tag, outputStream);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void closeAllOutputStreams() throws IOException {
		lock.writeLock().lock();
		try {
			for (String tag : outputStreamMap.keySet()) {
				OutputStreamStruct outputStream = outputStreamMap.get(tag);
				if (outputStream != null && outputStream.stream != null) {
					if (outputStream.bytesWritten == 0) {
						deleteOutputStream(outputStream);
					} else
						closeOutputStream(outputStream);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void deleteOutputStream(OutputStreamStruct outputStream) {
		try {
			outputStream.stream.close();
			new File(outputStream.filePath).delete();
		} catch (IOException e) {
			log.error("close file failed for delete", e);
		}
	}

	private OutputStreamStruct newOutputStream(String tag) throws IOException {
		OutputStreamStruct outputStream = new OutputStreamStruct();
		FileUtil.ensurePathExists(baseDir + "/" + tag);
		outputStream.filePath = filenameGenerator.generateFileName(baseDir, tag);
		outputStream.stream = new FileOutputStream(outputStream.filePath, true);
		outputStream.bytesWritten = 0;
		outputStream.messagesWritten = 0;
		return outputStream;
	}

	private void closeOutputStream(OutputStreamStruct outputStream) throws IOException {
		outputStream.stream.close();
		new File(outputStream.filePath).renameTo(new File(outputStream.filePath.substring(0, outputStream.filePath.length() - 4)));
	}

	public OutputStreamStruct getOutputStream(String tag) throws IOException {
		OutputStreamStruct outputStream = null;
		lock.readLock().lock();
		try {
			outputStream = outputStreamMap.get(tag);
			if (outputStream != null && outputStream.bytesWritten > MAX_BYTES_PER_FILE) {
				closeOutputStream(outputStream);
				outputStream = null;
			}
			if (outputStream == null || outputStream.stream == null) {
				outputStream = newOutputStream(tag);
				outputStreamMap.put(tag, outputStream);
			}
			return outputStream;
		} finally {
			lock.readLock().unlock();
		}
	}
}
