package com.taobao.timetunnel2.router.biz;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.taobao.timetunnel.thrift.router.Constants;
import com.taobao.timetunnel.thrift.router.ExReason;
import com.taobao.timetunnel.thrift.router.RouterException;
import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.loadbalance.LoadBalancer;
import com.taobao.timetunnel2.router.loadbalance.LoadBalancerFactory;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public class BizRouterServiceTest {
	private BizRouterService brs;
	private RouterContext context;

	@Before
	public void setUp() throws Exception {
		context = RouterContext.getContext();
		LoadBalancer policy1 = LoadBalancerFactory.getLoadBalancerPolicy("RoundRobinStatelessLoadBalancer");	
		context.setPolicy(ParamsKey.LBPolicy.policy, policy1);
		LoadBalancer policy2 = LoadBalancerFactory.getLoadBalancerPolicy("ConstantLoadBalancer");	
		context.setPolicy(ParamsKey.LBPolicy.s_policy, policy2);
		brs = new BizRouterService(context);
	}

	@After
	public void tearDown() throws Exception {
		context.cleanup();
	}

	@Test
	public void testGetBroker() {
		try {			
			
			Map<String, String> prop = new HashMap<String, String>();
			prop.put(Constants.LOCAL_HOST, "host2:9090");
			prop.put(Constants.TIMEOUT, "100");
			prop.put(Constants.RECVWINSIZE, "200");
			prop.put(Constants.TYPE, "PUB");
			try{				
				brs.getBroker("tt", "2", "dfs", "0", prop);
			}catch(RouterException e){
				Assert.assertEquals(ExReason.NOTFOUND_BROKERURL.getValue(), e.code);
			}
			try{				
				brs.getBroker("tt", "3", "dfs", "1", prop);
			}catch(RouterException e){
				Assert.assertEquals(ExReason.INVALID_USERORPWD.getValue(), e.code);
			}
			try{				
				String s = brs.getBroker("tt", "2", "acookie", "1", prop);
				
				List<String> expected = new ArrayList<String>();
				expected.add("10.232.130.1:9999");
				expected.add("10.232.130.2:9999");
				expected.add("10.232.130.3:9999");
				
				Gson gson = new Gson();
				BrokerSrvRlt obj1 = gson.fromJson(new StringReader(s), BrokerSrvRlt.class);
				List<String> actual1 = obj1.getBrokerserver();
				String last = actual1.get(0);

				Assert.assertEquals(1, actual1.size());
				Assert.assertTrue(expected.containsAll(actual1));
				System.out.println("last="+last);
				s = brs.getBroker("tt", "2", "acookie", "1", prop);
				BrokerSrvRlt obj2 = gson.fromJson(new StringReader(s), BrokerSrvRlt.class);
				List<String> actual2 = obj2.getBrokerserver();
				System.out.println("new="+actual2.get(0));
				Assert.assertEquals(last, actual2.get(0));
				
			}catch(RouterException e){
				Assert.assertEquals(ExReason.INVALID_USERORPWD, e.code);
			}			
		} catch (TException e) {
			e.printStackTrace();
		} 
	}

}
