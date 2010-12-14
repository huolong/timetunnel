package com.taobao.timetunnel2.router.zkclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.recipes.lock.LockListener;
import org.apache.zookeeper.recipes.lock.WriteLock;

import com.taobao.timetunnel2.cluster.util.StringUtil;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathChildDeleter;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathChildWatcher;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathChildWatcher.ChildrenVisitor;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathChildrenListener;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathCreator;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathDataGetter;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathDataSetter;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathDataWatcher;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathDataWatcher.DataVisitor;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathDeleter;
import com.taobao.timetunnel2.cluster.zookeeper.operation.PathExistChecker;

public class ZooKeeperRecyclableClient {
	public enum UpdateStatus {
		PENDING, OK;
	}
	
	private ZooKeeper zooKeeper;
	//private Thread watchThread;
	private Map<String, Thread> watchThreads = new HashMap<String, Thread>();
	private Map<String, DataVisitor> vistors = new HashMap<String, DataVisitor>();

	public ZooKeeperRecyclableClient(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
	}

	public void initPath(String path) {
		if (!new PathExistChecker(zooKeeper, path, false).exist()) {
			new PathCreator(zooKeeper, path, null, CreateMode.PERSISTENT)
					.create();
		} else {
			new PathChildDeleter(zooKeeper, path, ".*").delete();
		}
	}

	public void deletePathTree(String path) {
		if (new PathExistChecker(zooKeeper, path, false).exist()) {
			new PathChildDeleter(zooKeeper, path, ".*").delete();
			new PathDeleter(zooKeeper, path).delete();
		}
	}

	public void deletePath(String path) {
		new PathDeleter(zooKeeper, path).delete();
	}

	public void deletePathChild(String path, String filter) {
		new PathChildDeleter(zooKeeper, path, ".*").delete();
	}

	public boolean existPath(String path, boolean blocking) {
		return new PathExistChecker(zooKeeper, path, blocking).exist();
	}

	public void createPath(String path, byte[] data, CreateMode createMode) {
		new PathCreator(zooKeeper, path, data, createMode).create();
	}

	public void createPath(String path, CreateMode createMode) {
		new PathCreator(zooKeeper, path, null, createMode).create();
	}

	public void createPathRecursively(String path, CreateMode createMode) {
		String[] pathTokens = path.split("/");
		String pathToBeCreated = "";
		for (String token : pathTokens) {
			if (token.equals(""))
				continue;
			pathToBeCreated += "/";
			pathToBeCreated += token;
			createPath(pathToBeCreated, createMode);
		}
	}

	public List<String> listPathChildren(String path) {
		return new PathChildrenListener(zooKeeper, path).getChildren();
	}

	public void watchPathChild(final String path, final ChildrenVisitor visitor) {
		new Thread() {
			public void run() {
				new PathChildWatcher(zooKeeper, path, visitor).watch();
			}
		}.start();
	}

	public void watchPathData(final String path, final DataVisitor visitor) {
		Thread watchThread = new Thread() {
			public void run() {
				new PathDataWatcher(zooKeeper, path, visitor).watch();
			}
		};		
		vistors.put(path, visitor);
		watchThreads.put(path, watchThread);
		watchThread.start();
		/*watchThread = new Thread() {
			public void run() {
				new PathDataWatcher(zooKeeper, path, visitor).watch();
			}
		};
		watchThread.start();*/
	}

	public boolean lockPath(String path, LockListener listener) {
		try {
			return new WriteLock(zooKeeper, path, null, listener).lock();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (KeeperException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	public byte[] getPathData(String path) {
		return new PathDataGetter(zooKeeper, path).getData();
	}

	public boolean setPathData(String path, byte[] data) {
		return new PathDataSetter(zooKeeper, path, data).setData();
	}

	public String getPathDataAsStr(String path) {
		byte[] data = new PathDataGetter(zooKeeper, path).getData();
		if (data == null)
			return "";
		else
			return new String(data);
	}

	public boolean setPathDataAsStr(String path, String data) {
		byte[] dataBytes = null;
		if (data != null)
			dataBytes = StringUtil.str2Bytes(data);
		return new PathDataSetter(zooKeeper, path, dataBytes).setData();
	}

	public String getPathLatestChild(String path) {
		String value = "";
		List<String> children = this.listPathChildren(path);
		if (children == null || children.size() == 0)
			return value;
		int maxIndex = -1;
		for (String child : children) {
			String[] tokens = StringUtil.split(child);
			int index = Integer.valueOf(tokens[tokens.length - 1]);
			if (index > maxIndex) {
				maxIndex = index;
				value = StringUtil.lastSplit(child)[0];
			}
		}
		return value;
	}

	public ZooKeeper getZooKeeper() {
		return zooKeeper;
	}

	public void close() {
		try {
			/*if (watchThread != null)
				watchThread.interrupt();*/
			if(watchThreads!=null){
				for(Thread watchThread: watchThreads.values()){
					if (watchThread!=null)
						watchThread.interrupt();
				}
			}
			getZooKeeper().close();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public void reconnect(ZooKeeper zooKeeper) {
		try {
			getZooKeeper().close();
			this.zooKeeper = zooKeeper;			
			if(watchThreads!=null){
				for(Entry<String, Thread> entry: watchThreads.entrySet()){
					String path = entry.getKey();
					Thread watchThread = entry.getValue();
					if (watchThread!=null){
						watchThread.interrupt();	
						DataVisitor visitor = vistors.get(path);
						if (visitor!=null)
							watchPathData(path, visitor);
					}
				}
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
