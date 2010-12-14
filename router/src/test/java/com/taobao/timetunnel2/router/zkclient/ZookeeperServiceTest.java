package com.taobao.timetunnel2.router.zkclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.zookeeper.KeeperException.NoNodeException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.loadbalance.Context;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public class ZookeeperServiceTest {
	private ZookeeperServiceAgent zks;
	
	@Before
	public void setUp() throws Exception {
		
		Properties prop = new Properties();
		try {			
			prop.load(ClassLoader.getSystemClassLoader().getResourceAsStream(RouterConsts.ROUTER_PATH));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ZookeeperProperties zprops = new ZookeeperProperties(prop);
		try {
			Context context = RouterContext.getContext();
			Map<String, String> paths = new HashMap<String, String>();
			paths.put(ParamsKey.ZNode.topic, "d");
			paths.put(ParamsKey.ZNode.user, "d");
			paths.put(ParamsKey.ZNode.broker, "c");
			zks = new ZookeeperServiceAgent(zprops, context, paths);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		zks.close();
	}

	@Test
	public void testDoServe() {
		zks.doServe();		
	}
	
	@Test
	public void testDelete() {
		try {
			List<String> brokers = zks.getChildren(ParamsKey.ZNode.broker);
			for(String group: brokers){
				try {
					List<String> nodes = zks.getChildren(ParamsKey.ZNode.broker+"/"+group);
					for(String node: nodes){
						zks.delete(ParamsKey.ZNode.broker+"/"+group+"/"+ node,true);	
					}
				} catch (NoNodeException e) {
					e.printStackTrace();
				}
				zks.delete(ParamsKey.ZNode.broker+"/"+group,true);	
			}
		} catch (NoNodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}	
	
	@Test
	public void testSetData() {
		zks.setData(ParamsKey.ZNode.user+"/tt", "3");
		Assert.assertEquals("3", zks.getData(ParamsKey.ZNode.user+"/tt"));
		zks.setData(ParamsKey.ZNode.user+"/tt", "2");
		Assert.assertEquals("2", zks.getData(ParamsKey.ZNode.user+"/tt"));
		zks.setData(ParamsKey.ZNode.broker+"/group1/b0000000000", "{\"external\":9999,\"internal\":9998,\"host\":\"10.232.130.1\"}");
		Assert.assertEquals("{\"external\":9999,\"internal\":9998,\"host\":\"10.232.130.1\"}", zks.getData(ParamsKey.ZNode.broker+"/group1/b0000000000"));
		zks.setData(ParamsKey.ZNode.broker+"/group1/b0000000001", "{\"external\":9999,\"internal\":9998,\"host\":\"10.232.130.2\"}");
		Assert.assertEquals("{\"external\":9999,\"internal\":9998,\"host\":\"10.232.130.2\"}", zks.getData(ParamsKey.ZNode.broker+"/group1/b0000000001"));
		zks.setData(ParamsKey.ZNode.broker+"/group1/b0000000002", "{\"external\":9999,\"internal\":9998,\"host\":\"10.232.130.3\"}");
		Assert.assertEquals("{\"external\":9999,\"internal\":9998,\"host\":\"10.232.130.3\"}", zks.getData(ParamsKey.ZNode.broker+"/group1/b0000000002"));
	}
	
	@Test
	public void testGetChildren() {
		try {
			List<String> topics = zks.getChildren(ParamsKey.ZNode.topic);
			for(String topic: topics){
				System.out.println(topic);
			}			
			
		} catch (NoNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetData() {
		String pwd = zks.getData(ParamsKey.ZNode.user+"/"+"tt");
		Assert.assertEquals("2", pwd);
	}

	@Test
	public void testFinish() {
		zks.finish();
	}

}
