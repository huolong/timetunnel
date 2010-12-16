package com.taobao.timetunnel2.client.dsl.message;

public interface IOSerializable {
	
	void deserialize(byte[] bytes);
	
	byte[] serialize();

}
