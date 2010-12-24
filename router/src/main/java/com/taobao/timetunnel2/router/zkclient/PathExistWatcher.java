package com.taobao.timetunnel2.router.zkclient;

import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.recipes.lock.ProtocolSupport;
import org.apache.zookeeper.recipes.lock.ZooKeeperOperation;

public class PathExistWatcher extends ProtocolSupport {
	private static final Logger log = Logger.getLogger(PathExistWatcher.class);

	public interface PathVisitor {
		public void getChangePath(String path);
	}

	final private String path;
	final private ZooKeeper zooKeeper;
	final private Visitor visitor;

	public PathExistWatcher(ZooKeeper zooKeeper, String path, Visitor visitor) {
		super(zooKeeper);
		this.zooKeeper = zooKeeper;
		this.path = path;
		this.visitor = visitor;
	}

	private class WatchDataOperation implements ZooKeeperOperation<Boolean>,
			Watcher {
		private final Semaphore semaphore = new Semaphore(1);
		private String eventPath;
		private EventType eventType;
		public void process(WatchedEvent event) {
			log.debug("exists event =>" + event);
			eventPath = event.getPath();
			eventType = event.getType();
			if (event.getType() == Event.EventType.None) {
				if (event.getState() == KeeperState.SyncConnected) {
					semaphore.release();
				}
			}else if (eventType == EventType.NodeCreated ||
				eventType == EventType.NodeDeleted ||
				eventType == EventType.NodeDataChanged)
				semaphore.release();
		}

		public Boolean execute() throws KeeperException, InterruptedException {
			while (!Thread.currentThread().isInterrupted()) {
				semaphore.acquire();
				if (eventPath==null){	
					zooKeeper.exists(path, this);
				}else{
					zooKeeper.exists(eventPath, this);
				}
				if(eventType==EventType.NodeCreated)
					visitor.onNodeCreated(eventPath);
				else if(eventType==EventType.NodeDeleted)
					visitor.onNodeDeleted(eventPath);
				else if (eventType==EventType.NodeDataChanged){
					visitor.onNodeDataChanged(eventPath);
				}
				
			}
			return true;
		}
	};

	public void watch() {
		try {
			this.retryOperation(new WatchDataOperation());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (KeeperException e) {
			throw new RuntimeException(e);
		}
	}
}