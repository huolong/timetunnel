package com.taobao.timetunnel2.router.loadbalance;

public interface LoadBalancer {	
	String choose(String topic, String clientId);
}
