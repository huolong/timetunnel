package com.taobao.timetunnel2.router.zkclient;

import java.util.List;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.KeeperException.NoNodeException;

public interface ZookeeperService {

	public String getData(String path);
	
	public String getData(String path, DataCallback cb, Object ctx);

	public List<String> getChildren(String path) throws NoNodeException;
	
	public List<String> getChildren(String path, ChildrenCallback cb, Object ctx) throws NoNodeException;

	public void setData(String path, String value);
	
	public void setData(String path, String value, StatCallback cb, Object ctx);
	
	public void delete(String path, boolean cascade);	

	public void delete(String path, boolean cascade, VoidCallback cb, Object ctx);	
	
	public void close();
}
