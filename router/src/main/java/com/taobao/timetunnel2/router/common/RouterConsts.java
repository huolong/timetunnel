package com.taobao.timetunnel2.router.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class RouterConsts {
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	public static final String APP_PATH = System.getProperty("user.dir");

	public static final String LOG_PATH = "log4j.properties";//APP_PATH + FILE_SEPARATOR + "conf"+ FILE_SEPARATOR + "log4j.properties";
	
	public static final String ROUTER_PATH = "router.properties";//APP_PATH + FILE_SEPARATOR + "conf"+ FILE_SEPARATOR + "router.properties";
	
	public static final String ERRMSG_AUTH_FAIL = "Authentication is failed.";
	
	public static final String ERRMSG_NO_SERVER = "Can't find broker server list.";
	
	public static final String ERRMSG_UNAVAILABLE = "Router server is unavailable.";
	
	public static final int EXPIRED_PERIOD = 24*60*60;
	
	public static final String LB_APPLY = "1";	
	
	public static final String WATCH_MODE_SETDATA = "d";
	
	public static final String WATCH_MODE_CHILDCHANGE = "c";
	
	public static final String ID_SPLIT = "-"; 
	
	public static void main(String[] args) {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File(ROUTER_PATH)));
			
			System.out.println(prop.getProperty(ParamsKey.ZKService.hosts));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
