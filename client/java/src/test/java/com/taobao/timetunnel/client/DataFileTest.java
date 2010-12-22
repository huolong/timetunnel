package com.taobao.timetunnel.client;

import java.io.File;
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.taobao.timetunnel.client.disk.DataFile;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-13
 * 
 */
public class DataFileTest {

	@Test
	public void createAndDelete() throws Exception {
		DataFile dataFile = new DataFile("target/DataFileTest");
		String queueFileName = dataFile.getQueueFileName();
		assertThat(queueFileName, equalTo(null));
		System.out.println(queueFileName);
		assertThat(dataFile.isEmpty(), equalTo(true));
		System.out.println(dataFile.isEmpty());
		queueFileName = dataFile.createQueueFile();
		RandomAccessFile randomAccessFile = new RandomAccessFile(new File(queueFileName), "rwd");
		randomAccessFile.close();
		queueFileName = dataFile.createQueueFile();
		randomAccessFile = new RandomAccessFile(new File(queueFileName), "rwd");
		randomAccessFile.close();
		queueFileName = dataFile.createQueueFile();
		System.out.println(queueFileName);
		assertThat(queueFileName, endsWith("2"));
		System.out.println(dataFile.isEmpty());
		assertThat(dataFile.isEmpty(), equalTo(false));
		randomAccessFile = new RandomAccessFile(new File(queueFileName), "rwd");
		randomAccessFile.close();
		System.out.println(dataFile.getQueueFileName());
		dataFile.deleteOlderFiles(queueFileName);

	}

	@After
	public void clear() {
		File file = new File("target/DataFileTest");
		for (File f : file.listFiles()) {
			f.delete();
		}
		file.delete();
	}
}
