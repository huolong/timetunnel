package com.taobao.timetunnel2.router.biz;

import java.util.List;

public class BrokerSrvRlt {
	private String sessionId;
	private List<String> brokerserver;
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public List<String> getBrokerserver() {
		return brokerserver;
	}
	public void setBrokerserver(List<String> brokerserver) {
		this.brokerserver = brokerserver;
	}
	
}

