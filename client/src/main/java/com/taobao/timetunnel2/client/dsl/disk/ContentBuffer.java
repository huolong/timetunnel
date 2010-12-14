package com.taobao.timetunnel2.client.dsl.disk;

import java.nio.ByteBuffer;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-18
 * 
 */
public class ContentBuffer {
	private ByteBuffer bf;
	private int limit;
	private volatile boolean active = false;

	public ContentBuffer() {
		this.limit = 0;
		this.bf = null;
	}

	public void allocate(int len) {
		setActive(true);
		this.limit = len;
		bf = ByteBuffer.allocate(len);
	}

	public ByteBuffer get() {
		return bf;
	}

	public int position() {
		return bf.position();
	}

	public void put(byte b) {
		bf.put(b);
	}

	public void put(ByteBuffer src) {
		bf.put(src);
	}

	public int remaining() {
		return limit - bf.position();
	}

	public void flip() {
		bf.flip();
	}

	public void clear() {
		setActive(false);
		bf.clear();
	}

	public int getLimit() {
		return limit;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
