package com.taobao.timetunnel.client.message;

public interface IOSerializable {
	
	void deserialize(byte[] bytes);
	
	byte[] serialize();

}
