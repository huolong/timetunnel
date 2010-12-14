package com.taobao.timetunnel2.router.zkclient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public abstract class AutoReconnectZookeeperService implements Watcher {
	private static final Logger log = Logger
			.getLogger(AutoReconnectZookeeperService.class);

	private ZooKeeper zooKeeper;
	private ZooKeeperRecyclableClient zooKeeperClient;
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
			reInit();
		}
	}
	
	public AutoReconnectZookeeperService(){
		this("vm-dev1.sds1.corp.alimama.com:5181", 3000);
	}
	
	public AutoReconnectZookeeperService(String serverList, int sessionTimeout) {
		this.serverList = serverList;
		this.sessionTimeout = sessionTimeout;
	}
	
	public void init() {
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
	
	public void setConnectedSignal(CountDownLatch connectedSignal) {
		this.connectedSignal = connectedSignal;
	}

	public void reInit() {
		log.info("Auto-reconnection to ZooKeeper cluster is starting...");
		try {
			zooKeeper = new ZooKeeper(serverList, sessionTimeout, this);
			zooKeeperClient.reconnect(zooKeeper);
			connectedSignal.await();
		} catch (IOException e) {
			throw new RuntimeException("Fail to reconnect to zookeeper cluster "
					+ serverList, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}		
		log.info("A new connection to ZooKeeper cluster has been established succeessfully.");
	}
	
	public void close() {
		zooKeeperClient.close();
		zooKeeperClient = null;
		zooKeeper = null;
	}

	protected void cleanup() {
		shutdown();
	}

	protected void shutdown() {
		log.error("Shutdown...");
		System.exit(-1);
	}

	public ZooKeeperRecyclableClient getZooKeeperClient() {
		return zooKeeperClient;
	}

	public abstract void serve();

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

}
