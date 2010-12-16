package com.taobao.timetunnel2.router.loadbalance;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoadBalancerFactoryTest {

	@Before
	public void setUp() throws Exception {
		RouterContext.getContext();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetLoadBalancerPolicy() {
		LoadBalancer instance = (LoadBalancer) LoadBalancerFactory.getLoadBalancerPolicy("RoundRobinLoadBalancer");
		String exp = instance.choose("acookie", "host1:8080");
		assertEquals(exp, "10.232.130.1:9999");
	}

}
