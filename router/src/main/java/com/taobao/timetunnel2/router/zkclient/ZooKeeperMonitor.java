package com.taobao.timetunnel2.router.zkclient;

public interface ZooKeeperMonitor {
	public enum WatchType {
		ChildrenChanged, DataChanged;
	}
	public void doServe();
	public void finish();
}
