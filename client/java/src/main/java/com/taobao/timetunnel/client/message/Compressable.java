package com.taobao.timetunnel.client.message;

public interface Compressable {

	void compress();

	void decompress();

	boolean isCompressed();

}
