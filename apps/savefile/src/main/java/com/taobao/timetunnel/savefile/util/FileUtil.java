package com.taobao.timetunnel.savefile.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.log4j.Logger;

public class FileUtil {

	private static final Logger log = Logger.getLogger(FileUtil.class);

	private static class DirectoryLister extends DirectoryWalker {

		private String dateStr;

		public DirectoryLister(String dateStr) {
			super();
			this.dateStr = dateStr;
		}

		public DirectoryLister() {
			super();
			this.dateStr = null;
		}

		public List<File> list(File startDirectory,String filterRegex) throws IOException {
			List<File> dirList = new ArrayList<File>();
			walk(startDirectory, dirList);
			Collections.sort(dirList, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return (Long.valueOf(f1.lastModified())).compareTo(Long.valueOf(f2.lastModified()));
				}
			});
			List<File> filteredDirList=new ArrayList<File>();
			for (File f : dirList)
				if (f.getName().matches(filterRegex))
					filteredDirList.add(f);
			return filteredDirList;
		}

		@SuppressWarnings("unchecked")
		protected void handleFile(File file, int depth, Collection dirList) {
			if (dateStr == null) {
				if (!file.getName().endsWith(".tmp"))
					dirList.add(file);
			} else {
				if (!file.getName().endsWith(".tmp")
						&& file.getName().compareToIgnoreCase(dateStr) <= 0
						|| file.getName().startsWith(dateStr))
					dirList.add(file);
			}
		}
	}

	public static List<File> listDirectory(String path,String filterRegex) {
		try {
			return new DirectoryLister().list(new File(path),filterRegex);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<File> listDirectory(String path, String dateStr,String filterRegex) {
		log.debug("list dir: " + path + " and dateStr: " + dateStr);
		try {
			return new DirectoryLister(dateStr).list(new File(path),filterRegex);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String ensurePathExists(String path) {
		File f = new File(path);
		if (!f.exists())
			f.mkdirs();
		return path;
	}

	public static boolean rename(File f1, File f2) {
		ensurePathExists(f2.getParent());
		if (f2.exists())
			f2.delete();
		return f1.renameTo(f2);
	}
}
