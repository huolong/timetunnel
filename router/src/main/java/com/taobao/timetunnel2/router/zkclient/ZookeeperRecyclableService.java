package com.taobao.timetunnel2.router.zkclient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public abstract class ZookeeperRecyclableService implements Watcher{
	private static final Logger log = Logger
			.getLogger(ZookeeperRecyclableService.class);

	private ZooKeeper zooKeeper;
	private ZooKeeperRecyclableClient zooKeeperClient;
	private String serverList;
	private int sessionTimeout;
	private CountDownLatch connectedSignal = new CountDownLatch(1);
	private DataMonitorListener listener;
	
	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == Event.EventType.None) {
			if (event.getState() == KeeperState.SyncConnected)
				connectedSignal.countDown();
			else if (event.getState() == KeeperState.Expired) {
				log.info("Connection to ZooKeeper cluster is lost permenantly");
				this.setConnectedSignal(new CountDownLatch(1));
				reconnect();
			}
		}else{
			processDataEvent(event);
		}
	}
	
	public ZookeeperRecyclableService(){
		this("vm-dev1.sds1.corp.alimama.com:5181", 3000, null);
	}
	
	public ZookeeperRecyclableService(String serverList, int sessionTimeout){
		this(serverList, sessionTimeout, null);
	}
	
	public ZookeeperRecyclableService(String serverList, int sessionTimeout, DataMonitorListener listener) {
		this.serverList = serverList;
		this.sessionTimeout = sessionTimeout;
		this.listener = listener;
	}
	
	public void setConnectedSignal(CountDownLatch connectedSignal) {
		this.connectedSignal = connectedSignal;
	}
	
	public void connect() {
		try {
			zooKeeper = new ZooKeeper(serverList, sessionTimeout, this);
			zooKeeperClient = new ZooKeeperRecyclableClient(zooKeeper);
			connectedSignal.await();
		} catch (IOException e) {
			throw new RuntimeException("Fail to connect to zookeeper cluster "
					+ serverList, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public void diconnect() {
		try {
			if (zooKeeper != null) 
				zooKeeper.close();
			if (zooKeeperClient != null)
				zooKeeperClient.close();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
	        throw new RuntimeException(e);
		} finally{
			zooKeeper = null;
			zooKeeperClient = null;
		}
	}
	
	public void reconnect() {
		try {
			zooKeeper = new ZooKeeper(serverList, sessionTimeout, this);
			zooKeeperClient.reconnect(zooKeeper);
			connectedSignal.await();
		} catch (IOException e) {
			throw new RuntimeException("Fail to connect to zookeeper cluster "
					+ serverList, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
	}

	protected void cleanup() {		
		shutdown();
	}

	protected void shutdown() {
		log.error("Shutdown...");
		System.exit(-1);
	}
	
	protected void processDataEvent(WatchedEvent event){
		if(listener!=null){
			EventType type = event.getType();
			String path = event.getPath();
			switch(type){
				case NodeCreated: 
					listener.onNodeCreated(path);
					break;
				case NodeDeleted:
					listener.onNodeDeleted(path);
					break;
				case NodeDataChanged:
					listener.onNodeDataChanged(path);
					break;
				case NodeChildrenChanged:
					listener.onNodeChildrenChanged(path);
					break;
				default: break;
			}
		}
	}

	public void sleep() {
		Object o = new Object();
		synchronized (o) {
			try {
				o.wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public ZooKeeperRecyclableClient getZooKeeperClient() {
		return zooKeeperClient;
	}
	
	public String getServerList() {
		return serverList;
	}

	public void setServerList(String serverList) {
		this.serverList = serverList;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
	
	public interface DataMonitorListener {

	    void onNodeChildrenChanged(String path);

	    void onNodeCreated(String path);

	    void onNodeDataChanged(String path);

	    void onNodeDeleted(String path);

	  }
}
