package com.taobao.timetunnel2.router.zkclient;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;

import com.taobao.timetunnel2.cluster.zookeeper.operation.PathCreator;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathExistChecker;

public class AsyncZookeeperClient extends RecyclableZookeeperService implements AsyncZookeeperService {
	
	public AsyncZookeeperClient(ZookeeperProperties zkprop){
		super(zkprop.getZkSrvList(),zkprop.getZkTimeout());
		this.connect();
	}
	
	@Override
	public void getData(String path, DataCallback cb, Object ctx) {
		this.getZooKeeper().getData(path, false, cb, ctx);
	}

	@Override
	public void getChildren(String path, ChildrenCallback cb, Object ctx)
			throws NoNodeException {
		this.getZooKeeper().getChildren(path, false, cb, ctx);
	}

	@Override
	public void setData(String path, String value, StatCallback cb, Object ctx) {
		if(!existPath(path))
			createPathRecursively(path, CreateMode.PERSISTENT);
		this.getZooKeeper().setData(path, value.getBytes(), -1, cb, ctx);		
	}
	
	
	private void createPath(String path, CreateMode createMode) {
		new PathCreator(this.getZooKeeper(), path, null, createMode).create();
	}

	private void createPathRecursively(String path, CreateMode createMode) {
		String[] pathTokens = path.split("/");
		String pathToBeCreated = "";
		for (String token : pathTokens) {
			if (token.equals(""))
				continue;
			pathToBeCreated += "/";
			pathToBeCreated += token;
			createPath(pathToBeCreated, createMode);
		}
	}
	
	private boolean existPath(String path) {
		return new PathExistChecker(this.getZooKeeper(), path, false).exist();
	}

	@Override
	public void delete(String path, boolean casecade, VoidCallback cb,
			Object ctx) {
		this.getZooKeeper().delete(path, -1, cb, ctx);		
	}

	@Override
	public void finish() {
		this.close();		
	}


}
