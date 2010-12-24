package com.taobao.timetunnel2.router.loadbalance;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel.thrift.router.Constants;
import com.taobao.timetunnel2.router.exception.ServiceException;

public class RouterContextTest extends TestCase{
	private RouterContext context; 
	@Before
	public void setUp() throws Exception {
		context = RouterContext.getContext();
	}

	@After
	public void tearDown() throws Exception {
		context.cleanup();
	}

	@Test
	public void testSync() {
		context.sync();
	}

	@Test
	public void testGetSessionStats() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetSessionStats() {
		//fail("Not yet implemented");
	}

	@Test
	public void testAuthenticate() {
		String session = null;
		try {
			Map<String, String> prop = new HashMap<String, String>();
			prop.put(Constants.LOCAL_HOST, "host2:8080");
			prop.put(Constants.TIMEOUT, "80");
			prop.put(Constants.TYPE, "PUB");			
			session = context.authenticate("tt", "2", "acookie", prop);
			assertEquals(33, session.substring(session.lastIndexOf("/")+1,session.length()).length());
			session = context.authenticate("tt", "3", "acookie", prop);
			assertEquals(null, session);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testCleanup() {
		//context.cleanup();
	}

}
