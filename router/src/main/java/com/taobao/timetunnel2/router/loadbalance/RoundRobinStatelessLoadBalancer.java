package com.taobao.timetunnel2.router.loadbalance;


public class RoundRobinStatelessLoadBalancer implements LoadBalancer {
	
	private RouterMap routerMap = RouterMap.getInstance();

	public synchronized String choose(String topic, String clientId) {
		return routerMap.getCurrent(topic);		
	}
}
