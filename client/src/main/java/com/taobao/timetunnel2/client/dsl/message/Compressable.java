package com.taobao.timetunnel2.client.dsl.message;

public interface Compressable {

	void compress();

	void decompress();

	boolean isCompressed();

}
