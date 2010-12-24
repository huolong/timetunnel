package com.taobao.timetunnel.savefile.util;
import java.nio.*;
public class BytesUtil {
	public static byte[] intToBytes(int i){
		ByteBuffer bb = ByteBuffer.allocate(4); 
		bb.putInt(i); 
		return bb.array();
	}
	public static int bytesToInt(byte[] intBytes){
		ByteBuffer bb = ByteBuffer.wrap(intBytes);
		return bb.getInt();
	}

}
