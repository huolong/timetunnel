package com.taobao.timetunnel2.router.zkclient;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;


public class ZooKeeperExector extends ZookeeperRecyclableService implements ZookeeperService{
	
	private ZooKeeperRecyclableClient zkc = null;
	
	public ZooKeeperExector(ZookeeperProperties zProps){
		super(zProps.getZkSrvList(), zProps.getZkTimeout());		
		this.connect();
		this.zkc = getZooKeeperClient();
	}
	
	public boolean isAlive(){
		return zkc.getZooKeeper().getState().isAlive();
	}
	
	private void checkOpened(){
		if(!this.isAlive())
			this.reconnect();
	}
	
	@Override
	public String getData(String path) {
		checkOpened();
		return zkc.getPathDataAsStr(path);
	}

	@Override
	public String getData(String path, DataCallback cb, Object ctx) {
		GetDataCallback dcb = new GetDataCallback();
		CountDownLatch signal = new CountDownLatch(1);
		zkc.getZooKeeper().getData(path, false, cb, signal);
		return dcb.getResult();
	}

	@Override
	public List<String> getChildren(String path) throws NoNodeException {
		checkOpened();
		return zkc.listPathChildren(path);
	}

	@Override
	public List<String> getChildren(String path, ChildrenCallback cb, Object ctx)
			throws NoNodeException {
		CCallback ccb = new CCallback(); 
		CountDownLatch signal = new CountDownLatch(1);
		zkc.getZooKeeper().getChildren(path, false, ccb, signal);	
		return ccb.getResult();
	}

	@Override
	public void setData(String path, String value) {
		checkOpened();
		if(!zkc.existPath(path, false))
			zkc.createPathRecursively(path, CreateMode.PERSISTENT);
		zkc.setPathDataAsStr(path, value);
	}

	@Override
	public void setData(String path, String value, StatCallback cb, Object ctx) {
		if(!zkc.existPath(path, false))
			zkc.createPathRecursively(path, CreateMode.PERSISTENT);
		CountDownLatch signal = new CountDownLatch(1);
		SetDataCallBack cbk = new SetDataCallBack();
		zkc.getZooKeeper().setData(path, value.getBytes(), -1, cbk, signal);		
		try {
			signal.await(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void delete(String path, boolean cascade) {
		checkOpened();
		if(cascade)
			zkc.deletePathTree(path);
		else
			zkc.deletePath(path);
	}

	@Override
	public void delete(String path, boolean cascade, VoidCallback cb,
			Object ctx) {
		VVoidCallback vcb = new VVoidCallback();
		CountDownLatch signal = new CountDownLatch(1);
		zkc.getZooKeeper().delete(path, -1, vcb, signal);		
	}

	@Override
	public void close() {
		checkOpened();
		zkc.close();	
	}
	
	class VVoidCallback implements VoidCallback{
		@Override
		public void processResult(int rc, String path, Object ctx) {
			CountDownLatch signal = (CountDownLatch)ctx;
			Code code = Code.get(rc);
			if (code.equals(Code.OK)) {
			}else if (code.equals(Code.SESSIONEXPIRED)) {
				reconnect();	
			}
			signal.countDown();			
		}		
	}
	
	class GetDataCallback implements DataCallback{
		private String data;
		public String getResult(){
			return data;
		}
		@Override
		public void processResult(int rc, String path, Object ctx,
				byte[] data, Stat stat) {
			CountDownLatch signal = (CountDownLatch)ctx;
			Code code = Code.get(rc);
			if (code.equals(Code.OK)) {
				if (data!=null)
					this.data = new String(data);
			}else if (code.equals(Code.SESSIONEXPIRED)) {
				reconnect();	
			}
			signal.countDown();
		}		
	}

	class SetDataCallBack implements StatCallback{
		@Override
		public void processResult(int rc, String path, Object ctx, Stat stat) {
			CountDownLatch signal = (CountDownLatch)ctx;
			Code code = Code.get(rc);	
			if (code.equals(Code.OK)) {
			}else if (code.equals(Code.SESSIONEXPIRED)) {
				reconnect();
			}else{				
			}
			signal.countDown();
		}		
	}
	
	class CCallback implements ChildrenCallback{
		private List<String> dirs; 		
		@Override
		public void processResult(int rc, String path, Object ctx,
				List<String> dirs) {
			CountDownLatch count = (CountDownLatch)ctx;
			Code code = Code.get(rc);
			if (code.equals(Code.OK)) {				
				this.dirs = dirs;
			} else if (code.equals(Code.NONODE)) {
				this.dirs = null;
			} else if (code.equals(Code.SESSIONEXPIRED) || code.equals(Code.CONNECTIONLOSS)) {
				reconnect();
			} else if (code.equals(Code.NOAUTH)) {
				return;
			} else {
			}		
			count.countDown();
		}	
		
		public List<String> getResult(){
			return dirs;
		}
	}

}
