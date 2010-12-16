package com.taobao.timetunnel2.router.loadbalance;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.timetunnel2.router.biz.BrokerUrl;
import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.zkclient.ZooKeeperClientPool;
import com.taobao.timetunnel2.router.zkclient.ZookeeperService;

public class RouterMap {
	private final static RouterMap instance = new RouterMap();
	private ConcurrentHashMap<String, RouterCircle> routerMap = new ConcurrentHashMap<String, RouterCircle>();
	private ConcurrentHashMap<String, String> clientMap = new ConcurrentHashMap<String, String>();
	private ZooKeeperClientPool zkpool = ZooKeeperClientPool.getInstance();
	private ZookeeperService client = zkpool.getZooKeeperClient();
	public static RouterMap getInstance() {
		return instance;
	}
	
	public synchronized List<String> getRouters(String topic){
		if (routerMap==null)
			return null;
		RouterCircle circle = routerMap.get(topic);
		if(circle==null)
			return null;		
		return circle.getNodes();		
	}
	
	public synchronized void update(String topic, List<BrokerUrl> brokers){
		if(topic == null || brokers==null)
			return;
		RouterCircle circle = new RouterCircle(topic);
		circle.createCircleRouter(brokers);
		if (circle.getCount()>0){
			routerMap.put(topic, circle);
		}
	}
	
	public void setClientStatus(String clientId, String brokerUrl){
		clientMap.put(clientId, brokerUrl);
	}
	
	public void setClientConstStatus(String clientId, String brokerUrl){
		ZookeeperService client = zkpool.getZooKeeperClient();
		client.setData(ParamsKey.ZNode.status+"/"+clientId, brokerUrl);
	}
	
	public void clearClientStatus(String clientId){
		clientMap.remove(clientId);
		client.delete(ParamsKey.ZNode.status+"/"+clientId, true);
	}
	
	public void changeClientStatus(Collection<String> newBrokers){	
		clientMap.values().retainAll(newBrokers);
	}
	
	public String getClientStatus(String clientId){
		return clientMap.get(clientId);
	}
	
	public String getClientConstStatus(String clientId){
		return client.getData(ParamsKey.ZNode.status+"/"+clientId);
	}
	
	public String getFollower(String topic, String broker){
		if (topic == null || broker == null )
			return null;
		RouterCircle circle = routerMap.get(topic);
		if (circle == null)
			return null;
		return circle.getFollowerNode(broker);	
	}
	
	public String getCurrent(String topic){		
		RouterCircle circle =  routerMap.get(topic);
		if (circle == null)
			return null;
		return circle.getCurrentNodeAndNext();
	}
	
	public void clear(){
		routerMap.clear();		
	}
	
	public void clearAll(){
		routerMap = null;
		clientMap = null;
	}

}
