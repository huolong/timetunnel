package com.taobao.timetunnel.client.util;

import java.nio.ByteBuffer;

public class BytesUtil {

	public static byte[] toBytes(ByteBuffer bf) {
		if (bf.position() == 0 && bf.limit() == bf.capacity()) {
			return (byte[]) bf.array();
		}
		int position = bf.position();
		int len = bf.limit() - bf.position();
		byte[] ret = new byte[len];
		bf.get(ret, 0, len);
		bf.position(position);
		return ret;
	}

}
