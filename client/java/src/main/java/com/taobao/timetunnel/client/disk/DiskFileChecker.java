package com.taobao.timetunnel.client.disk;

import static com.taobao.timetunnel.client.TimeTunnel.asString;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.TreeSet;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.message.MessageFactory;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-13
 * 
 */
public class DiskFileChecker {
	private static String path;
	private static long pos;
	private static String name;

	private static void parseMeta() {
		try {
			RandomAccessFile f = new RandomAccessFile(path + "meta", "r");
			MappedByteBuffer mbb = f.getChannel().map(MapMode.READ_ONLY, 0, f.length());
			mbb.position(0);
			pos = mbb.getLong();
			long ck = mbb.getLong();
			int len = mbb.getInt();
			byte[] dst = new byte[len];
			mbb.get(dst);
			name = new String(dst, Charset.forName("UTF-8"));
			System.out.println("file: " + name + " pos: " + pos + " checksum: " + ck);
			mbb = null;
			f.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			pos = 0;
			name = "null";
		}
	}

	private static TreeSet<String> scanPath() {
		TreeSet<String> files = new TreeSet<String>();
		File dir = new File(path);
		if (!dir.exists())
			return files;
		String[] nameList = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("tt");
			}
		});
		for (String fileName : nameList) {
			String fullPath = path + fileName;
			files.add(fullPath);
		}
		System.out.println(files);
		return files;
	}

	private static void parseData() throws IOException {
		TreeSet<String> fs = scanPath();
		parseMeta();
		if (fs.size() == 0)
			return;
		String absolutePath = new File(fs.first()).getCanonicalPath();
		String absolutePath2 = new File(name).getCanonicalPath();
		if (!absolutePath.equals(absolutePath2)) {
			System.out.println("meta file name not match first file, use first file");
			pos = 0;
		}

		for (String filename : fs) {
			RandomAccessFile f = new RandomAccessFile(filename, "r");
			MappedByteBuffer map = f.getChannel().map(MapMode.READ_ONLY, 0, f.length());
			map.position((int) pos);
			while (map.hasRemaining()) {
				int size = map.getInt();
				byte[] c = new byte[size];
				map.get(c);
				Message m = MessageFactory.getInstance().createMessageFrom(c);
				m.decompress();
				System.out.println("get content: " + asString(m));
			}
			map.clear();
			map = null;
			f.close();
			pos = 0;
		}
	}

	public static void main(String[] args) throws IOException {
		path = args[0] + File.separator;
		parseData();
	}
}
