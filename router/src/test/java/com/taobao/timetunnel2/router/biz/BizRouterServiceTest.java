package com.taobao.timetunnel2.router.biz;

import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;
import com.taobao.timetunnel.thrift.router.Constants;
import com.taobao.timetunnel.thrift.router.RouterException;

public class BizRouterServiceTest {
	private BizRouterService brs;

	@Before
	public void setUp() throws Exception {
		RouterContext context = RouterContext.getContext();
		brs = new BizRouterService(context);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetBroker() {
		try {			
			
			Map<String, String> prop = new HashMap<String, String>();
			prop.put(Constants.LOCAL_HOST, "localhost");
			prop.put(Constants.RECVWINSIZE, "200");
			prop.put(Constants.TYPE, "PUB");
			String s = brs.getBroker("tt", "2", "dfs", "0", prop);
			
			Assert.assertEquals(RouterConsts.ERRMSG_NO_SERVER, s);
			s = brs.getBroker("tt", "3", "dfs", "1", prop);
			Assert.assertEquals(RouterConsts.ERRMSG_AUTH_FAIL, s);
			s = brs.getBroker("tt", "2", "test1", "0", prop);
			
			/*List<String> expected = new ArrayList<String>();
			expected.add("{\"master\":\"dwbasis130001.sqa.cm4:39903\",\"slave\":[]}");
			expected.add("{\"master\":\"2:39903\",\"slave\":[]}");
			
			Gson gson = new Gson();
			BrokerSrvRlt obj = gson.fromJson(new StringReader(s), BrokerSrvRlt.class);
			List<String> actual = obj.getBrokerserver();
			Assert.assertEquals(expected, actual);
			Assert.assertEquals(2, actual.size());
			Assert.assertTrue(expected.containsAll(actual));*/
			
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RouterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
