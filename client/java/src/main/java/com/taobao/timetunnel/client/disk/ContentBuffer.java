package com.taobao.timetunnel.client.disk;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-18
 * 
 */
public class ContentBuffer {
	private ByteBuffer bf;
	private int limit;
	private AtomicBoolean active;

	public ContentBuffer() {
		this.limit = 0;
		this.bf = null;
		this.active = new AtomicBoolean(false);
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
		// not real clear underline array
		bf.clear();
	}

	public int getLimit() {
		return limit;
	}

	public boolean isActive() {
		return active.get();
	}

	public void setActive(boolean active) {
		this.active.set(active);
	}

}
