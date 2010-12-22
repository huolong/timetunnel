package com.taobao.timetunnel.client.message;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-9
 * 
 */
public interface CompresssAlgo {

	String algoName();

	byte[] compress(byte[] input);

	byte[] decompress(byte[] input);
}
