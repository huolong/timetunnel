package com.taobao.timetunnel2.router.loadbalance;

public class RoundRobinLoadBalancer implements LoadBalancer {
	
	private RouterMap routerMap = RouterMap.getInstance();

	public synchronized String choose(String topic, String clientId) {
		String broker = routerMap.getClientStatus(clientId);
		if(broker != null)
			broker = routerMap.getFollower(topic, broker);			
		else
			broker = routerMap.getCurrent(topic);
		routerMap.setClientStatus(clientId, broker);
		return broker;		
	}
}
