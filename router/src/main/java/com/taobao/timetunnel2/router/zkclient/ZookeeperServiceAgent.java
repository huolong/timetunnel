package com.taobao.timetunnel2.router.zkclient;

import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;

import com.taobao.timetunnel2.cluster.zookeeper.operation.PathChildWatcher;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathDataWatcher;
import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.loadbalance.Context;

public class ZookeeperServiceAgent extends AutoReconnectZookeeperService implements
		ZookeeperService, PathDataWatcher.DataVisitor, PathChildWatcher.ChildrenVisitor{

	private ZooKeeperRecyclableClient zkc = null;
	private Context ctx = null;
	private Map<String,String> watchpaths = null;
	private String version = null;
	private int count=0;
	
	public ZookeeperServiceAgent(ZookeeperProperties zProps, Context ctx, Map<String,String> paths) {
		super(zProps.getZkSrvList(), zProps.getZkTimeout());		
		this.init();
		this.zkc = getZooKeeperClient();
		this.ctx = ctx;
		this.watchpaths = paths;
	}

	@Override
	public List<String> getChildren(String path) throws NoNodeException {	
		return zkc.listPathChildren(path);
	}

	@Override
	public String getData(String path) {
		return zkc.getPathDataAsStr(path);
	}

	@Override
	public void setData(String path, String value) {
		if(!zkc.existPath(path, false))
			zkc.createPathRecursively(path, CreateMode.PERSISTENT);
		zkc.setPathDataAsStr(path, value);
	}
	
	@Override
	public void delete(String path, boolean cascade){
		if(cascade)
			zkc.deletePathTree(path);
		else
			zkc.deletePath(path);
	}

	@Override
	public void serve() {		
		count = watchpaths.size();
		for(String key: watchpaths.keySet()){
			if(watchpaths.get(key).equalsIgnoreCase(RouterConsts.WATCH_MODE_CHILDCHANGE)){
				try {
					List<String> childrenpaths = getChildren(key);
					for(String childpath:childrenpaths){						
						getZooKeeperClient().watchPathChild(key+"/"+childpath, this);
					}
				} catch (NoNodeException e) {
					;
				}				
			}
			else
				getZooKeeperClient().watchPathData(key, this);
		}		
	}

	@Override
	public void finish() {
		zkc.close();		
	}

	@Override
	public void visitData(String path, byte[] data) {
		if(count>0){
			count--;
			return;
		}		
		String _version = new String(data);
		if (version!=null){			
			if (version.equals(_version))
				return;
		}else{
			version = _version;
			return;
		}
		ctx.sync();
	}
	
	@Override
	public void visitChildren(String path, List<String> children) {
		if(count>0){
			count--;
			return;
		}
		ctx.sync();
	}
	
	@Override
	public void doServe() {
		serve();
	}


}
