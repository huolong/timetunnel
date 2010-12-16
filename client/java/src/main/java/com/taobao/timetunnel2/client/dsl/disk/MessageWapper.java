package com.taobao.timetunnel2.client.dsl.disk;

import java.nio.ByteBuffer;

import com.taobao.timetunnel2.client.dsl.Message;
import com.taobao.timetunnel2.client.dsl.message.MessageFactory;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-14
 * 
 */
public class MessageWapper {
	private final ByteBuffer bf;
	private final boolean firstMInFile;
	private final long startPos;
	private final String currentFileName;

	public MessageWapper(ContentBuffer content, boolean firstMInFile, long startPos, String currentFileName) {
		super();
		this.bf = content.get();
		this.firstMInFile = firstMInFile;
		this.startPos = startPos;
		this.currentFileName = currentFileName;
	}

	public Message getM() {
		return MessageFactory.getInstance().createMessageFrom(bf.array());
	}

	public ByteBuffer getBf() {
		return bf;
	}

	public boolean isFirstMInFile() {
		return firstMInFile;
	}

	public long getStartPos() {
		return startPos;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}

}
