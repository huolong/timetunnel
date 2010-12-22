package com.taobao.timetunnel.client.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.impl.Config;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-13
 * 
 */
public class FCQMeta {

	static class Meta {
		private volatile long readPos;
		private volatile long checkSum;
		private volatile String fileName;

		public Meta(long readPos) {
			super();
			this.readPos = readPos;
			fileName = "null";
			checkSum = Utils.checkSum(concat());
		}

		public Meta(long readPos, String fileName) {
			super();
			this.readPos = readPos;
			this.fileName = fileName;
			checkSum = Utils.checkSum(concat());
		}

		public Meta() {
			super();
			this.readPos = 0L;
			fileName = "null";
			checkSum = Utils.checkSum(concat());
		}

		public long getReadPos() {
			return readPos;
		}

		public void set(long readPos, String fileName) {
			this.readPos = readPos;
			this.fileName = fileName;
			this.checkSum = Utils.checkSum(concat());
		}

		private String concat() {
			return Utils.concat(this.readPos, this.fileName);
		}

		public String getFileName() {
			return fileName;
		}

		public long getCheckSum() {
			return checkSum;
		}
	}

	private static Logger log = Logger.getLogger(FCQMeta.class);
	private final Meta meta;
	private final String name = "meta";
	private MappedByteBuffer mbb;
	private final int maxFileLen = 2048;
	private final RandomAccessFile metaFile;

	public FCQMeta(String queueName) {
		super();
		this.meta = new Meta();
		String metaPath = Config.getInstance().getMetaPath() + File.separator + queueName + File.separator;
		ensurePath(metaPath);
		try {
			metaFile = new RandomAccessFile(metaPath + this.name, "rwd");
			this.mbb = metaFile.getChannel().map(MapMode.READ_WRITE, 0, maxFileLen);
		} catch (Exception e) {
			throw new RuntimeException("construct CFQMetaOnDisk failed", e);
		}
		loadFromDisk();
	}

	private void ensurePath(String path) {
		File dir = new File(path);
		if (!dir.exists())
			if (!dir.mkdirs())
				throw new RuntimeException("can not create dir: " + path);
	}

	private void loadFromDisk() {
		mbb.position(0);
		long pos = mbb.getLong();
		long ck = mbb.getLong();
		int len = mbb.getInt();
		if (len > maxFileLen - 20) {
			log.error("file content currupt. So set position to 0 and file to null");
			meta.set(0, "null");
			return;
		}
		byte[] dst = new byte[len];
		try {
			mbb.get(dst);
		} catch (BufferUnderflowException e) {
			log.error("file content currupt. So set position to 0 and file to null");
			meta.set(0, "null");
			return;
		}
		String name = new String(dst, Charset.forName("UTF-8"));
		validateMeta(pos, name, ck);
	}

	private void validateMeta(long pos, String fileName, long ck) {
		long ck2 = Utils.checkSum(Utils.concat(pos, fileName));
		if (ck2 != ck) {
			log.error("check sum from disk not correct " + ck + " should be " + ck2 + ". So set position to 0 and file to null");
			meta.set(0, "null");
		} else {
			meta.set(pos, fileName);
		}
	}

	public synchronized void update(long pos, String fileName) {
		meta.set(pos, fileName);
		mbb.position(0);
		mbb.putLong(meta.getReadPos());
		mbb.putLong(meta.getCheckSum());
		byte[] bytes = fileName.getBytes(Charset.forName("UTF-8"));
		mbb.putInt(bytes.length);
		mbb.put(bytes);
	}

	public long getReadPos() {
		return meta.getReadPos();
	}

	public String getFileName() {
		return meta.getFileName();
	}

	public synchronized void close() {
		mbb.force();
		mbb = null;
		try {
			metaFile.close();
		} catch (IOException e) {
			log.error("close metaFile error", e);
		}
	}
}
