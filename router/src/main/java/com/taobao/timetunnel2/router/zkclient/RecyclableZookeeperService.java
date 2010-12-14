package com.taobao.timetunnel2.router.zkclient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public abstract class RecyclableZookeeperService implements Watcher{
	private static final Logger log = Logger
			.getLogger(RecyclableZookeeperService.class);

	private ZooKeeper zooKeeper;
	private String serverList;
	private int sessionTimeout;
	private CountDownLatch connectedSignal = new CountDownLatch(1);
	
	@Override
	public void process(WatchedEvent event) {
		if (event.getState() == KeeperState.SyncConnected)
			connectedSignal.countDown();
		else if (event.getState() == KeeperState.Expired) {
			log.info("Connection to ZooKeeper cluster is lost permenantly");
			this.setConnectedSignal(new CountDownLatch(1));
			reconnect();
		}
	}
	
	public RecyclableZookeeperService(){
		this("vm-dev1.sds1.corp.alimama.com:5181", 3000);
	}
	
	public RecyclableZookeeperService(String serverList, int sessionTimeout) {
		this.serverList = serverList;
		this.sessionTimeout = sessionTimeout;
	}
	
	public void connect() {
		try {
			zooKeeper = new ZooKeeper(serverList, sessionTimeout, this);
			connectedSignal.await();
		} catch (IOException e) {
			throw new RuntimeException("Fail to connect to zookeeper cluster "
					+ serverList, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public void setConnectedSignal(CountDownLatch connectedSignal) {
		this.connectedSignal = connectedSignal;
	}

	public void reconnect() {
		close();
		connect();
	}
	
	public void close() {
		try {
			if (zooKeeper != null) 
				zooKeeper.close();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
	        throw new RuntimeException(e);
		} finally{
			zooKeeper = null;
		}
	}

	protected void cleanup() {		
		shutdown();
	}

	protected void shutdown() {
		log.error("Shutdown...");
		System.exit(-1);
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
	
	public ZooKeeper getZooKeeper() {
		return zooKeeper;
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

}
