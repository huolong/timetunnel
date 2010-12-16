package com.taobao.timetunnel2.router.zkclient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
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
    	private List<String> prevData;
    	public void process(WatchedEvent event)  {
			eventType = event.getType();
			eventPath = event.getPath();
			log.debug("getChildren event =>"+event);
			if (eventType == EventType.NodeChildrenChanged || eventType== EventType.NodeDataChanged)
    			semaphore.release();
    	}
    	public Boolean execute() throws KeeperException, InterruptedException {
    				while (true) {
    					semaphore.acquire();
    					List<String> children = null;
    					
    					try{
    						if (eventPath==null){
    							System.out.println("path change watcher=" + path);
    							children = zooKeeper.getChildren(path, this);
    						}else{
    							System.out.println("path change watcher=" + path);
    							children = zooKeeper.getChildren(eventPath, this);
    							//create
    							if ((prevData == null && children!=null && children.size()>0) ||
    	    							(prevData != null && children!=null && children.size()>prevData.size())){
    								log.debug("path change add watcher=" + eventPath);
    	    						List<String> arry = new ArrayList<String>(); 
    	    						arry.addAll(children);
    	    						arry.removeAll(prevData);    	    						
    	    						zooKeeper.getChildren(eventPath+"/"+arry.get(0), this);   						
    	    					}
    	    					if (eventType == EventType.NodeChildrenChanged){
    	    						visitor.onNodeChildrenChanged(eventPath, children);
    	    						
    	    					}   							
    						}
    						prevData = children;
    					}catch(NoNodeException e){    						
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