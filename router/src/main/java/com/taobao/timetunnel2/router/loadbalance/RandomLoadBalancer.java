package com.taobao.timetunnel2.router.loadbalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer implements LoadBalancer {
	
	private RouterMap routerMap = RouterMap.getInstance();

	public synchronized String choose(String topic, String clientId) {
		String broker = routerMap.getClientStatus(clientId);
		if(broker == null){
			List<String> routers = routerMap.getRouters(topic);
			Random random = new Random();
			int size = 0;
			if (routers != null && (size = routers.size()) > 0) {
				int index = random.nextInt(size);
				if (index < size) {
					broker = routers.get(index);
				}
			}					
		}
		routerMap.setClientStatus(clientId, broker);
		
		return broker;
		
	}
}
