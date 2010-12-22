package com.taobao.timetunnel.client.disk;

import java.nio.ByteBuffer;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.util.ClosedException;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-14
 * 
 */
public interface FileChannelQueue {

	public ByteBuffer get();

	public void add(Message m) throws ClosedException;

	public void close();
}
