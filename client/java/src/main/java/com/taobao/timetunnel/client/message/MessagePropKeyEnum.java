package com.taobao.timetunnel.client.message;

public enum MessagePropKeyEnum {
	COMPRESSED("c"), COMPRESSALGO("algo");

	private String token;

	private MessagePropKeyEnum(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}
}
