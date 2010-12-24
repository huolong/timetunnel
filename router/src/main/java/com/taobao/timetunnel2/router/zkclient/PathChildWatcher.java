package com.taobao.timetunnel2.router.zkclient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.recipes.lock.ProtocolSupport;
import org.apache.zookeeper.recipes.lock.ZooKeeperOperation;
public class PathChildWatcher extends ProtocolSupport {
	private static final Logger log=Logger.getLogger(PathChildWatcher.class);

	final private String path;
	final private ZooKeeper zooKeeper;
	final private Visitor visitor;
	public PathChildWatcher(ZooKeeper zooKeeper,String path,Visitor visitor) {
		super(zooKeeper);
		this.zooKeeper=zooKeeper;
		this.path=path;
		this.visitor=visitor;
	}
	
    private  class WatchPathOperation implements ZooKeeperOperation<Boolean>,Watcher{
    	private final Semaphore semaphore=new Semaphore(1);
		private EventType eventType;
		private String eventPath;
		private Map<String, List<String>> prevDataMap = new HashMap<String, List<String>>();
    	public void process(WatchedEvent event)  {
			eventType = event.getType();
			eventPath = event.getPath();
			log.debug("getChildren event =>"+event);
			if (event.getType() == Event.EventType.None) {
				if (event.getState() == KeeperState.SyncConnected) {
					semaphore.release();
				}
			}else if (eventType == EventType.NodeChildrenChanged || eventType== EventType.NodeDataChanged )
    			semaphore.release();
    	}
    	public Boolean execute() throws KeeperException, InterruptedException {
			while (true) {
				semaphore.acquire();
				List<String> children = null;

				if (eventPath == null) {
					if(prevDataMap.isEmpty()){
						System.out.println("path change watcher=" + path);
						try {
							children = zooKeeper.getChildren(path, this);
						} catch (NoNodeException e) {
						}
						prevDataMap.put(path, children);
					}else{
						for(String recoveryPath: prevDataMap.keySet()){
							try {
								System.out.println("path change watcher=" + recoveryPath);
								children = zooKeeper.getChildren(recoveryPath, this);
							} catch (NoNodeException e) {
							}	
							prevDataMap.put(recoveryPath, children);
						}
					}
					
				} else {
					System.out.println("path change watcher=" + eventPath);
					int newcount = 0;
					int precount = 0;
					try {
						children = zookeeper.getChildren(eventPath, this);
						if (children != null)
							newcount = children.size();
					} catch (NoNodeException e) {
					}
					List<String> prevData = prevDataMap.get(eventPath);
					if (prevData != null)
						precount = 0;
					// creates a child under the node and add a new watcher
					if (newcount > precount) {
						log.debug("path change add watcher=" + eventPath);
						List<String> arry = new ArrayList<String>();
						arry.addAll(children);
						arry.removeAll(prevData);
						List<String> newchildren = null;
						for (String childnode : arry) {
							try {
								newchildren = zookeeper.getChildren(eventPath
										+ "/" + childnode, this);
							} catch (NoNodeException e) {
							}
							if (newchildren != null)
								prevDataMap.put(eventPath + "/" + childnode, newchildren);
						}
					}
					prevDataMap.put(eventPath, children);
					if (eventType == EventType.NodeChildrenChanged) {
						visitor.onNodeChildrenChanged(eventPath, children);
					}
				}
			}
    	}
    };
    public void watch() {
    	try {
    		this.retryOperation(new WatchPathOperation());
     	}
    	catch (InterruptedException e) {
    		Thread.currentThread().interrupt();
    	}
    	catch (KeeperException e) {
    		throw new RuntimeException(e);
    	}
    }
}