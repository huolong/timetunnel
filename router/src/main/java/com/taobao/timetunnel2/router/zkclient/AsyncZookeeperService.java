package com.taobao.timetunnel2.router.zkclient;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.KeeperException.NoNodeException;

public interface AsyncZookeeperService {

	public void getData(String path, DataCallback cb, Object ctx);

	public void getChildren(String path, ChildrenCallback cb, Object ctx) throws NoNodeException;

	public void setData(String path, String value, StatCallback cb, Object ctx);

	public void delete(String path, boolean casecade, VoidCallback cb, Object ctx);
	
	public void finish();
}
