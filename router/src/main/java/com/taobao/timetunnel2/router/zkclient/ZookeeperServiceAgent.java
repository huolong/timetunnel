package com.taobao.timetunnel2.router.zkclient;

import java.util.List;
import java.util.Map;

import com.taobao.timetunnel2.router.common.RouterConsts;

public class ZookeeperServiceAgent extends ZookeeperRecyclableService implements ZooKeeperMonitor{

	private Map<String,String> watchpaths = null;
	private Visitor visitor;
	
	public ZookeeperServiceAgent(ZookeeperProperties zProps, Map<String,String> paths, Visitor visitor) {
		super(zProps.getZkSrvList(), zProps.getZkTimeout());	
		this.connect();
		this.watchpaths = paths;
		this.visitor = visitor;
	}
	@Override
	public void doServe() {
		for(String key: watchpaths.keySet()){
			if(watchpaths.get(key).equalsIgnoreCase(RouterConsts.WATCH_MODE_CHILDCHANGE)){
				List<String> childrenpaths = getZooKeeperClient().listPathChildren(key);
				if (childrenpaths != null && childrenpaths.size() > 0) {
					for (String childpath : childrenpaths) {
						getZooKeeperClient().watchPathChild(key + "/" + childpath, visitor);
					}
					getZooKeeperClient().watchPathChild(key, visitor);
					getZooKeeperClient().watchPathExist(key, visitor);
				}
			}
			else
				getZooKeeperClient().watchPathExist(key, visitor);
		}		
	}
	
	@Override
	public void finish() {
		this.diconnect();
	}
}
