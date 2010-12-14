package com.taobao.timetunnel2.router.zkclient;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.loadbalance.Context;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public class AsyncZookeeperClientTest {
	private AsyncZookeeperClient client;
	@Before
	public void setUp() throws Exception {
		Properties prop = new Properties();		
		System.out.println(this.getClass().getClassLoader().getResourceAsStream(RouterConsts.ROUTER_PATH));
		prop.load(this.getClass().getClassLoader().getResourceAsStream(RouterConsts.ROUTER_PATH));
		ZookeeperProperties zprops = new ZookeeperProperties(prop);
		client = new AsyncZookeeperClient(zprops);
	}
	
	@After
	public void tearDown() throws Exception {
		client.finish();
	}
	
	@Test
	public void testGetData() {
		DCallback dcb = new DCallback();
		CountDownLatch count = new CountDownLatch(1);
		client.getData("/categories/acookie", dcb, count);
		try {
			count.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("testGetData [/categories/acookie] result="+new String(dcb.getResult()));
	}

	@Test
	public void testGetChildren() {
		CCallback ccb = new CCallback();
		try {
			CountDownLatch count = new CountDownLatch(1);
			client.getChildren("/clients/host2:8080-acookie", ccb, count);
			count.await();
			List<String> dirs = ccb.getResult();
			for(String d: dirs){
				VCallback vcb = new VCallback();
				client.delete("/clients/host2:8080-acookie/"+d, false, vcb, null);
			}
		} catch (NoNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	//@Test
	public void testSetData() {
		fail("Not yet implemented");
	}

	@Test
	public void testDelete() {		
		VCallback vcb = new VCallback();
		client.delete("/clients/host1:8080-acookie/646d90b39f1d0774104863882f1f5c81", false, vcb, null);
	}
	
	class VCallback implements VoidCallback{

		@Override
		public void processResult(int rc, String arg1, Object ctx) {
			System.out.println(rc+",deleted.");			
		}
		
	}
	
	class CCallback implements ChildrenCallback{
		private List<String> dirs; 
		
		@Override
		public void processResult(int rc, String path, Object ctx,
				List<String> dirs) {			
			this.dirs = dirs;
			CountDownLatch count = (CountDownLatch)ctx;
			count.countDown();
		}	
		
		public List<String> getResult(){
			return dirs;
		}

	}
	
	class DCallback implements DataCallback{
		private byte[] data; 
		
		@Override
		public void processResult(int rc, String path, Object ctx,
				byte[] data, Stat stat) {			
			System.out.println(rc+"******,"+stat.toString()+"**********"+new String(data)+"*********");	
			this.data = data;
			CountDownLatch count = (CountDownLatch)ctx;
			count.countDown();
		}
		
		public byte[] getResult(){
			return data;
		}
	}

}
