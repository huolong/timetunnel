package com.taobao.timetunnel.client.disk;

import java.nio.ByteBuffer;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-15
 * 
 */
public class PreAllocate {
	private ByteBuffer bf;
	private static final double factor = 1.5;

	public PreAllocate() {
		this.bf = null;
	}

	public ByteBuffer allocate(int len) {
		if (this.bf == null || this.bf.capacity() < len) {
			int cap = (int) (len * factor);
			this.bf = ByteBuffer.allocate(cap);
		}
		this.bf.clear();
		return this.bf;
	}

	public static void main(String[] args) {
		PreAllocate preAllocate = new PreAllocate();
		ByteBuffer s = preAllocate.allocate(10);
		System.out.println(s.limit());
		s.put("hello".getBytes());
		System.out.println(s.limit());
		s.flip();
		System.out.println(s.limit());
		System.out.println(s.position());
	}
}
