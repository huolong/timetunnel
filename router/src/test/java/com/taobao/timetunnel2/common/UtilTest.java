package com.taobao.timetunnel2.common;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.taobao.timetunnel2.router.biz.BrokerSrvRlt;
import com.taobao.timetunnel2.router.common.Util;

public class UtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToJsonStr() {
		BrokerSrvRlt b = new BrokerSrvRlt();
		List<String> l = new ArrayList<String>();
		
		l.add("{\"master\":\"Xentest1-vm15.corp.alimama.com:8000\",\"slave\":[]}");
		l.add("{\"master\":\"Xentest1-vm15.corp.alimama.com:8080\",\"slave\":[]}");
		l.add("{\"master\":\"Xentest1-vm15.corp.alimama.com:9000\",\"slave\":[]}");
		b.setSessionId(Util.getMD5("tt|acookie"));
		b.setBrokerserver(l);
		System.out.println(Util.toJsonStr(b));
		System.out.println(Util.getMD5("tt|acookie"));
		
		Gson gson = new Gson();
		BrokerSrvRlt obj = gson.fromJson(new StringReader(Util.toJsonStr(b)), BrokerSrvRlt.class);
		System.out.println(obj.getSessionId());
		List<String> ll = obj.getBrokerserver();
		for(String bb:ll){
			System.out.println(bb);
		}
	}

	@Test
	public void testGetMD5() {
		System.out.println(Util.getMD5("tt|acookie"));
	}

}
