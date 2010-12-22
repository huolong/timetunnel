package com.taobao.timetunnel.client.impl;

import com.taobao.timetunnel.client.util.JsonUtils;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-25
 * 
 */
public class Authentication {
	private String username;
	private String password;

	public Authentication(String name, String password) {
		super();
		this.username = name;
		this.password = password;
	}

	public Authentication() {
	}

	public String getUserName() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String toJson() {
		return JsonUtils.json(this);
	}

	@Override
	public String toString() {
		return "username: " + username + "\npwd: " + password;
	}

	public static Authentication fromJson(String jsonStr) {
		return JsonUtils.parse(jsonStr).toAInstance(Authentication.class);
	}

	public static void main(String[] args) {
		System.out.println(new Authentication("jason", "111").toJson());
		System.out.println(Authentication.fromJson("{\"username\":\"jason\",\"password\":\"111\"}"));
	}
}
