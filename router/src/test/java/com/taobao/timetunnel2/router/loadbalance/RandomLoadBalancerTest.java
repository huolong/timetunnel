package com.taobao.timetunnel2.router.loadbalance;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.common.ParamsKey;

public class RandomLoadBalancerTest extends TestCase{
	private RouterContext handler;
	@Before
	public void setUp() throws Exception {
		handler = RouterContext.getContext();
		LoadBalancer policy = LoadBalancerFactory.getLoadBalancerPolicy("RandomLoadBalancer");	
		handler.setPolicy(ParamsKey.LBPolicy.policy,policy);		
	}

	@After
	public void tearDown() throws Exception {
		handler.cleanup();
	}

	@Test
	public void testChoose() {
		LoadBalancer policy = handler.getPolicy(ParamsKey.LBPolicy.policy);
		Map<String, Integer> accumulator = new HashMap<String, Integer>();
		for(int i=0; i<1000; i++){			
			counter(accumulator,policy.choose("acookie", "host"+i));
		}
		for(String key: accumulator.keySet()){
			System.out.println("key="+key+",count="+accumulator.get(key));
		}
	}
	
	@Test
	public void testFailOverChoose() {
		LoadBalancer policy = handler.getPolicy(ParamsKey.LBPolicy.policy);
		//host1-connect
		String oldbroker = policy.choose("acookie", "host1");
		String newbroker1 = policy.choose("acookie", "host1");
		String newbroker2 = policy.choose("acookie", "host1");
		Assert.assertEquals(oldbroker, newbroker1);
		Assert.assertEquals(oldbroker, newbroker2);
	}
	
	private void counter(Map<String, Integer> accumulator, String key){
		Integer value= accumulator.get(key);
		if(value!=null)			
			accumulator.put(key, value.intValue()+1);
		else
			accumulator.put(key, 1);
	}

}
