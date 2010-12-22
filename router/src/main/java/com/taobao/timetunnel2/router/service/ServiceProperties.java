package com.taobao.timetunnel2.router.service;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.Util;
import com.taobao.timetunnel2.router.common.ValidationException;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public class ServiceProperties {
	private static final Logger log = Logger.getLogger(ServiceProperties.class);
	private static final long serialVersionUID = 1L;

	private String id = "Untitled Service Instance";	

	private String name = "Untitled Service Name";

	private String bindAddr = "localhost"; 

	private int port = 9090;
	private int cliTimeout = 5000;

	private int minWorkerThreads = 5;

	private int maxWorkerThreads = Integer.MAX_VALUE;
	private int stopTimeoutVal = 60;
	private TimeUnit stopTimeoutUnit = TimeUnit.SECONDS;
	private long maxReadBufferBytes = Long.MAX_VALUE;

	public ServiceProperties() throws ServiceException {
		loadConf();
	}
	
	private void loadConf() throws ServiceException{
		Properties prop = RouterContext.getContext().getAppParam();
		if(prop!=null){
			try {
				minWorkerThreads = Util.getIntParam(ParamsKey.Service.minThreads, 
						prop.getProperty(ParamsKey.Service.minThreads),	5, 1, 10000);
				maxWorkerThreads = Util.getIntParam(ParamsKey.Service.maxThreads, 
						prop.getProperty(ParamsKey.Service.maxThreads),	10000, 1, 10000);
				if (minWorkerThreads > maxWorkerThreads)
					throw new ValidationException(String.format(
						"%s is greater than %s!", minWorkerThreads, maxWorkerThreads));
				stopTimeoutVal = Util.getIntParam(ParamsKey.Service.stopTimeoutVal, 
						prop.getProperty(ParamsKey.Service.stopTimeoutVal),	0, 60, 10000);
				stopTimeoutUnit = TimeUnit.valueOf(prop.getProperty(
						ParamsKey.Service.stopTimeoutUnit, "SECONDS"));
				bindAddr = prop.getProperty(ParamsKey.Service.host, "localhost");
				port = Util.getIntParam(ParamsKey.Service.port, 
						prop.getProperty(ParamsKey.Service.port), 9090,	1025, 65534);
				cliTimeout = Util.getIntParam(ParamsKey.Service.cliTimeout,
						prop.getProperty(ParamsKey.Service.cliTimeout), 0, 0, Integer.MAX_VALUE);
				maxReadBufferBytes = Util.getLongParam(ParamsKey.Service.maxReadBufferBytes,
						prop.getProperty(ParamsKey.Service.maxReadBufferBytes),
						Long.MAX_VALUE, 0L, Long.MAX_VALUE);
			} catch (ValidationException e) {
				log.error(e.getMessage());
				System.exit(-1);
			} catch (Exception e){
				log.error(e);
				System.exit(-1);				
			}
		}		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBindAddr() {
		return bindAddr;
	}

	public void setBindAddr(String bindAddr) {
		this.bindAddr = bindAddr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getCliTimeout() {
		return cliTimeout;
	}

	public void setCliTimeout(int cliTimeout) {
		this.cliTimeout = cliTimeout;
	}

	public int getMinWorkerThreads() {
		return minWorkerThreads;
	}

	public void setMinWorkerThreads(int minWorkerThreads) {
		this.minWorkerThreads = minWorkerThreads;
	}

	public int getMaxWorkerThreads() {
		return maxWorkerThreads;
	}

	public void setMaxWorkerThreads(int maxWorkerThreads) {
		this.maxWorkerThreads = maxWorkerThreads;
	}

	public int getStopTimeoutVal() {
		return stopTimeoutVal;
	}

	public void setStopTimeoutVal(int stopTimeoutVal) {
		this.stopTimeoutVal = stopTimeoutVal;
	}

	public TimeUnit getStopTimeoutUnit() {
		return stopTimeoutUnit;
	}

	public void setStopTimeoutUnit(TimeUnit stopTimeoutUnit) {
		this.stopTimeoutUnit = stopTimeoutUnit;
	}

	public long getMaxReadBufferBytes() {
		return maxReadBufferBytes;
	}

	public void setMaxReadBufferBytes(long maxReadBufferBytes) {
		this.maxReadBufferBytes = maxReadBufferBytes;
	}    

}
