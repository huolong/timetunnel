package com.taobao.timetunnel.client.disk;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.impl.Config;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-13
 * 
 */
public class DataFile {
	private final static Logger log = Logger.getLogger(DataFile.class);
	private final String path;
	private final String baseName = "tt";
	private final TreeSet<String> files;
	private final TreeSet<String> oldFiles;

	public DataFile(String queueName) {
		this.path = Config.getInstance().getDataPath() + File.separator + queueName + File.separator;
		this.files = new TreeSet<String>();
		this.oldFiles = new TreeSet<String>();
		scanPath();
	}

	private void scanPath() {
		File dir = new File(this.path);
		if (!dir.exists())
			return;
		String[] nameList = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(baseName);
			}
		});
		for (String fileName : nameList) {
			String fullPath = this.path + fileName;
			files.add(fullPath);
		}
	}

	public synchronized String createQueueFile() {
		long count = 0;
		if (!files.isEmpty()) {
			String lastFileName = files.last();
			count = Long.valueOf(lastFileName.substring(lastFileName.lastIndexOf(".") + 1)).longValue() + 1;
		} else if (!oldFiles.isEmpty()) {
			String lastFileName = oldFiles.last();
			count = Long.valueOf(lastFileName.substring(lastFileName.lastIndexOf(".") + 1)).longValue() + 1;
		}
		ensureDir();
		String newFileName = this.path + this.baseName + "." + String.format("%019d", count);
		files.add(newFileName);
		return newFileName;
	}

	private void ensureDir() {
		File baseFile = new File(this.path);
		if (!baseFile.exists())
			if (!baseFile.mkdirs())
				throw new RuntimeException("can not create dir: " + this.path);
	}

	public synchronized String getQueueFileName() {
		String fullname = files.pollFirst();
		if (fullname != null)
			oldFiles.add(fullname);
		return fullname;
	}

	public synchronized void deleteOlderFiles(String filePath) {
		if (oldFiles.size() == 0)
			return;
		String first = oldFiles.first();
		if (first.equals(filePath))
			return;
		NavigableSet<String> subSet = oldFiles.subSet(first, true, filePath, false);
		Iterator<String> it = subSet.iterator();
		while (it.hasNext()) {
			String toD = it.next();
			if (new File(toD).delete() == false) {
				log.error(toD + " has not been delete suc");
			} else
				oldFiles.remove(toD);
		}
	}

	public synchronized boolean isEmpty() {
		return files.isEmpty();
	}

}
