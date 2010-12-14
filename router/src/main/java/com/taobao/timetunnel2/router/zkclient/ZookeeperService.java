package com.taobao.timetunnel2.router.zkclient;

import java.util.List;

import org.apache.zookeeper.KeeperException.NoNodeException;

public interface ZookeeperService {

	public String getData(String path);

	public List<String> getChildren(String path) throws NoNodeException;

	public void setData(String path, String value);
	
	public void delete(String path, boolean casecade);
	
	public void doServe();
	
	public void finish();
}
