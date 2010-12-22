package com.taobao.timetunnel.client.disk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-13
 * 
 */
public class Utils {
	public static long checkSum(long src) {
		long cs = 0;
		Throwable t = null;
		for (int i = 0; i < 3; i++) {
			try {
				cs = calcCheckSum(String.valueOf(src));
				return cs;
			} catch (IOException e) {
				t = e;
				continue;
			}
		}
		throw new RuntimeException("can not calc check sum: " + src, t);
	}

	public static long checkSum(String src) {
		long cs = 0;
		Throwable t = null;
		for (int i = 0; i < 3; i++) {
			try {
				cs = calcCheckSum(src);
				return cs;
			} catch (IOException e) {
				t = e;
				continue;
			}
		}
		throw new RuntimeException("can not calc check sum: " + src, t);
	}

	public static String concat(long pos, String name) {
		return pos + name;
	}

	private static long calcCheckSum(String src) throws IOException {
		byte[] sb = src.getBytes(Charset.forName("UTF-8"));
		InputStream in = new ByteArrayInputStream(sb);
		CheckedInputStream cis = new CheckedInputStream(in, new Adler32());
		byte[] b = new byte[512];
		while (cis.read(b) >= 0)
			;
		return cis.getChecksum().getValue();
	}

	public static void main(String[] args) throws IOException {
		System.out.println(checkSum(234));
		System.out.println(checkSum("234"));
	}
}
