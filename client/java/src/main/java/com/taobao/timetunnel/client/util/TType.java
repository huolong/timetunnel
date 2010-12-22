package com.taobao.timetunnel.client.util;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-15
 * 
 */
public enum TType {
	SUB(0), PUB(1);

	private int index;

	private TType(int x) {
		index = x;
	}

	public int getIndex() {
		return index;
	}

	public static void main(String[] args) {
		System.out.println(PUB.name());
	}
}
