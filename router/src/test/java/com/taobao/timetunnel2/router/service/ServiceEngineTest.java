package com.taobao.timetunnel2.router.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;
import com.taobao.timetunnel2.router.zkclient.AsyncZookeeperClient;
import com.taobao.timetunnel2.router.zkclient.ZookeeperProperties;

public class ServiceEngineTest {
	private  ServiceEngine srv;
	@Before
	public void setUp() throws Exception {
		Properties prop = RouterContext.getContext().getAppParam();
		String name = prop.getProperty(ParamsKey.Service.serverType, "BLOCK");
		String classname = ParamsKey.Service.serverClass.BLOCK.getClassname();
		try{
			classname = ParamsKey.Service.serverClass.valueOf(name).getClassname();
		}catch(IllegalArgumentException e){
			e.printStackTrace();
		}
		srv = ServiceEngine.getInstance(classname);
	}
	
	@After
	public void tearDown() throws Exception {
		srv.stop();
	}
	
	@Test
	public void testStart() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					srv.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));
		try {
			srv.start();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
