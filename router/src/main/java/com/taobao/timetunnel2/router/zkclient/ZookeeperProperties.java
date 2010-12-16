package com.taobao.timetunnel2.router.zkclient;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.Util;
import com.taobao.timetunnel2.router.common.ValidationException;

public class ZookeeperProperties{
	private static final Logger log = Logger.getLogger(ZookeeperProperties.class);
	private static final long serialVersionUID = 1L;
	
	private int zkTimeout;
	private String zkSrvList;
	private int poolSize;
	
	public ZookeeperProperties(Properties prop){
		if(prop!=null){			
			try {
				zkSrvList = prop.getProperty(ParamsKey.ZKService.hosts, "vm-dev1.sds1.corp.alimama.com:5181");
				zkTimeout = Util.getIntParam(ParamsKey.ZKService.timeout, 
						prop.getProperty(ParamsKey.ZKService.timeout),	3000, 1, 500000);
				poolSize = Util.getIntParam(ParamsKey.ZKService.timeout, 
						prop.getProperty(ParamsKey.ZKClient.size), 1, 1, 10000);
			} catch (ValidationException e) {
				log.error(e.getMessage());
				System.exit(-1);
			} catch (Exception e){
				log.error(e);
				System.exit(-1);				
			} 
		}
	}
	
	public int getZkTimeout() {
		return zkTimeout;
	}
	public void setZkTimeout(int zkTimeout) {
		this.zkTimeout = zkTimeout;
	}
	public String getZkSrvList() {
		return zkSrvList;
	}
	public void setZkSrvList(String zkSrvList) {
		this.zkSrvList = zkSrvList;
	}
	public int getPoolSize() {
		return poolSize;
	}
	
}
