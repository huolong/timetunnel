package com.taobao.timetunnel2.router.service;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

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
		new Thread(){
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Assert.assertTrue(srv.isStarted());
				try {
					srv.stop();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
		try {
			srv.start();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
