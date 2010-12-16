package com.taobao.timetunnel2.client.dsl.disk;

import java.nio.ByteBuffer;

import com.taobao.timetunnel2.client.dsl.Message;
import com.taobao.timetunnel2.client.dsl.util.ClosedException;

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
