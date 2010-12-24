package com.taobao.timetunnel2.router.zkclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;
import com.taobao.timetunnel2.router.zkclient.ZooKeeperMonitor.WatchType;

public class ZookeeperServiceTest extends TestCase{
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
			RouterContext context = RouterContext.getContext();
			Map<String, WatchType> watchpaths = new HashMap<String, WatchType>();
			watchpaths.put(ParamsKey.ZNode.topic, WatchType.DataChanged);
			watchpaths.put(ParamsKey.ZNode.user, WatchType.DataChanged);
			watchpaths.put(ParamsKey.ZNode.broker, WatchType.ChildrenChanged);
			zks = new ZookeeperServiceAgent(zprops, watchpaths, context);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		zks.finish();
	}

	@Test
	public void testDoServe() {
		zks.doServe();		
	}
	

	@Test
	public void testFinish() {
		zks.finish();
	}

}
