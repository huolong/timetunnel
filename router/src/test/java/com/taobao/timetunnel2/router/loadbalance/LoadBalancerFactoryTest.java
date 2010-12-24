package com.taobao.timetunnel2.router.loadbalance;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoadBalancerFactoryTest extends TestCase {
	RouterContext handler;
	@Before
	public void setUp() throws Exception {
		handler= RouterContext.getContext();		
	}

	@After
	public void tearDown() throws Exception {
		handler.cleanup();
	}

	@Test
	public void testGetLoadBalancerPolicy() {
		LoadBalancer instance = (LoadBalancer) LoadBalancerFactory.getLoadBalancerPolicy("ConstantLoadBalancer");
		String exp1 = instance.choose("acookie", "host1:8080");
		assertEquals(exp1, "10.232.130.1:9999");
		String exp2 = instance.choose("acookie", "host1:8080");
		assertEquals(exp2, "10.232.130.1:9999");
	}

}
