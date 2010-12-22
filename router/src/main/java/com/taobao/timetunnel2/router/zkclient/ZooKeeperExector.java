package com.taobao.timetunnel2.router.zkclient;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.KeeperException.NoNodeException;


public class ZooKeeperExector extends ZookeeperRecyclableService implements ZookeeperService {
	
	private ZooKeeperRecyclableClient zkc = null;
	
	public ZooKeeperExector(ZookeeperProperties zProps){
		super(zProps.getZkSrvList(), zProps.getZkTimeout());		
		this.connect();
		this.zkc = getZooKeeperClient();
	}
	
	@Override
	public String getData(String path) {
		return zkc.getPathDataAsStr(path);
	}

	@Override
	public void getData(String path, DataCallback cb, Object ctx) {
		zkc.getZooKeeper().getData(path, false, cb, ctx);
	}

	@Override
	public List<String> getChildren(String path) throws NoNodeException {
		return zkc.listPathChildren(path);
	}

	@Override
	public void getChildren(String path, ChildrenCallback cb, Object ctx)
			throws NoNodeException {
		zkc.getZooKeeper().getChildren(path, false, cb, ctx);
	}

	@Override
	public void setData(String path, String value) {
		if(!zkc.existPath(path, false))
			zkc.createPathRecursively(path, CreateMode.PERSISTENT);
		zkc.setPathDataAsStr(path, value);
	}

	@Override
	public void setData(String path, String value, StatCallback cb, Object ctx) {
		zkc.getZooKeeper().setData(path, value.getBytes(), -1, cb, ctx);	
		
		if(!zkc.existPath(path, false))
			zkc.createPathRecursively(path, CreateMode.PERSISTENT);
		zkc.getZooKeeper().setData(path, value.getBytes(), -1, cb, ctx);
	}

	@Override
	public void delete(String path, boolean cascade) {
		if(cascade)
			zkc.deletePathTree(path);
		else
			zkc.deletePath(path);
	}

	@Override
	public void delete(String path, boolean cascade, VoidCallback cb,
			Object ctx) {
		zkc.getZooKeeper().delete(path, -1, cb, ctx);		
	}

	@Override
	public void close() {
		zkc.close();	
	}


}
