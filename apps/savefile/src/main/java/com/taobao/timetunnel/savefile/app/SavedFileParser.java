package com.taobao.timetunnel.savefile.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.message.MessageFactory;
import com.taobao.timetunnel.savefile.util.BytesUtil;

public class SavedFileParser {

	public static void main(String[] args) throws IOException {
		convertDataFile("D:/2010_09_09_14_16#d06ca100-e29e-45f7-a9cf-9a6a01a1b487");
	}

	private static void convertDataFile(String path) throws IOException {

		RandomAccessFile srvRandomAccessFile = new RandomAccessFile(new File(path), "r");

		FileWriter fw = new FileWriter(new File(path + ".converted"));
		ByteBuffer dataBuffer = ByteBuffer.allocate(40 * 1024 * 1024);

		int size = srvRandomAccessFile.read(dataBuffer.array());
		dataBuffer.position(size);
		dataBuffer.flip();
		int count = 0;

		byte[] messLenBytes = new byte[4];
		while (dataBuffer.hasRemaining()) {
			dataBuffer.get(messLenBytes, 0, 4);
			int len = BytesUtil.bytesToInt(messLenBytes);
			System.out.println("the len from file in convertDataFile is " + len);
			byte[] messageBuffer = new byte[len];
			if (dataBuffer.remaining() < len) {
				System.out.println("incomplete message " + messageBuffer.length);
				break;
			}
			dataBuffer.get(messageBuffer, 0, len);
			// System.out.println("message Buffer:  "+messageBuffer);
			// System.out.println("messageBuffer len is "+
			// messageBuffer.length);
			// System.out.println("dataBuffer remain is "+
			// dataBuffer.remaining());
			try {
				Message m = MessageFactory.getInstance().createMessageFrom(messageBuffer);
				if (m.isCompressed())
					m.decompress();

				// System.out.println("the len of content is: "+m.getContent().length);
				//					
				// System.out.println("content is: "+m.getContent());
				// System.out.println("content is: "+m.getCreatedTime());
				// System.out.println("content is: "+m.getIpAddress());
				// System.out.println("content is: "+m.getTopic());
				// System.out.println("content is: "+m.getId());
				fw.write(new String(m.getContent(), "UTF-8"));
				fw.flush();
				count++;

			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println("Throwable messageBuffer len is " + messageBuffer.length);
				System.out.println("Throwable dataBuffer remain is " + dataBuffer.remaining());
				System.out.println("Throwable the remain incomplete byte is:" + messageBuffer);
				continue;
			}

		}
		System.out.println("the message in file total count: " + count);
		fw.close();

	}
}
