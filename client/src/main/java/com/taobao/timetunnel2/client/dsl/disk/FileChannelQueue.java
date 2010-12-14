package com.taobao.timetunnel2.client.dsl.disk;

import java.nio.ByteBuffer;

import com.taobao.timetunnel2.client.dsl.Message;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-14
 * 
 */
public interface FileChannelQueue {

	public ByteBuffer get();

	public void add(Message m);

	public void close();
}
