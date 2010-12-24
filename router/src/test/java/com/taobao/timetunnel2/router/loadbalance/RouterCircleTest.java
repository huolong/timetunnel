package com.taobao.timetunnel2.router.loadbalance;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.biz.BrokerUrl;

public class RouterCircleTest extends TestCase {
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateCircleRouter() {
		RouterCircle factory = new RouterCircle("topic1");
		List<BrokerUrl> brokers = new ArrayList<BrokerUrl>();
		BrokerUrl b1=new BrokerUrl();
		b1.setHost("host1");
		b1.setExternal("1001");
		b1.setId("b0001");
		brokers.add(b1);
		BrokerUrl b2=new BrokerUrl();
		b2.setHost("host2");
		b2.setExternal("1002");
		b2.setId("b0002");
		brokers.add(b2);
		BrokerUrl b3=new BrokerUrl();
		b3.setHost("host3");
		b3.setExternal("1003");
		b3.setId("b0003");
		brokers.add(b3);
		Map<String, RouterNode> routers = factory.createCircleRouter(brokers);
		for(RouterNode router: routers.values()){
			System.out.println("id:"+router.getNode()+",pre:"+router.getLeader()+",next:"+router.getFollower());
		}
		
		RouterCircle factory2 = new RouterCircle("topic2");
		List<BrokerUrl> brokers2 = new ArrayList<BrokerUrl>();
		BrokerUrl b4=new BrokerUrl();
		b4.setHost("host4");
		b4.setExternal("1004");
		b4.setId("b0004");
		brokers2.add(b4);
		BrokerUrl b5=new BrokerUrl();
		b5.setHost("host5");
		b5.setExternal("1005");
		b5.setId("b0005");
		brokers2.add(b5);
		Map<String, RouterNode> routers2 = factory2.createCircleRouter(brokers2);
		System.out.println("***************");
		for(RouterNode router: routers2.values()){
			System.out.println("id:"+router.getNode()+",pre:"+router.getLeader()+",next:"+router.getFollower());
		}
		Map<String, Map<String,RouterNode>> routerMap = new HashMap<String, Map<String,RouterNode>>();		
		routerMap.put("topic1", routers);
		routerMap.put("topic2", routers2);
		Map<String, String> clientMap = new HashMap<String, String>();
		clientMap.put("client1#topic1", "b0004");
		clientMap.put("client2#topic2", "b0003");
	}

}
