package com.taobao.timetunnel.client.impl;

import com.taobao.timetunnel.client.util.HostUtils;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-26
 * 
 */
public class Config {
	private String[] routerServerList;

	private int maxPostLen;

	private String metaPath;
	private String dataPath;
	private int maxLoadSize;
	private int maxWriteSize;
	private int readQueueSize;

	private String appName;

	private String compressClassFullName;

	private int rpcTimeOut;

	private static Config instance = new Config();

	public static Config getInstance() {
		return instance;
	}

	private Config() {
		diskConfig();
		postConfig();
		routerConfig();
		appNameConfig();
		compressAlgoConfig();
		rpcTimeOutConfig();
	}

	private void rpcTimeOutConfig() {
		String rpcTimeOutStr = System.getProperty("RPCTIMEOUT");
		if (rpcTimeOutStr == null || "".equals(rpcTimeOutStr)) {
			rpcTimeOut = 10000;
		} else {
			rpcTimeOut = Integer.parseInt(rpcTimeOutStr);
		}
	}

	private void compressAlgoConfig() {
		try {
			this.compressClassFullName = System.getProperty("CMPZNAME");
			if (this.compressClassFullName == null || "".equals(this.compressClassFullName.trim())) {
				this.compressClassFullName = "com.taobao.timetunnel.client.message.ZlibCompressAlgo";
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void appNameConfig() {
		try {
			this.appName = System.getProperty("APPNAME");
			if (this.appName == null || "".equals(this.appName.trim())) {
				this.appName = HostUtils.id();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void routerConfig() {
		try {
			String serverList = System.getProperty("ROUTER");
			if (serverList == null || "".equals(serverList)) {
				serverList = "localhost:9000";
			}
			this.routerServerList = serverList.split(",");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void postConfig() {
		String maxPostStr = System.getProperty("MAXPOSTLEN");
		if (maxPostStr == null || "".equals(maxPostStr)) {
			maxPostLen = 64 * 1024 * 1024;
		} else {
			maxPostLen = Integer.parseInt(maxPostStr);
		}
	}

	private void diskConfig() {
		metaPath = System.getProperty("METAPATH");
		if (metaPath == null) {
			metaPath = "./";
		}
		dataPath = System.getProperty("DATAPATH");
		if (dataPath == null) {
			dataPath = "./";
		}
		String maxLoadSizeStr = System.getProperty("MAXLOADSIZE");
		if (maxLoadSizeStr == null || "".equals(maxLoadSizeStr)) {
			maxLoadSize = 2* 1024 * 1024;
		} else {
			this.maxLoadSize = Integer.parseInt(maxLoadSizeStr);
		}
		String maxWriteSizeStr = System.getProperty("MAXFILESIZE");
		if (maxWriteSizeStr == null || "".equals(maxWriteSizeStr)) {
			maxWriteSize = 64 * 1024 * 1024;
		} else {
			this.maxWriteSize = Integer.parseInt(maxWriteSizeStr);
		}
		String readQueueSizeStr = System.getProperty("READQUEUESIZE");
		if (readQueueSizeStr == null || "".equals(readQueueSizeStr)) {
			readQueueSize = 10000;
		} else {
			this.readQueueSize = Integer.parseInt(readQueueSizeStr);
		}
	}

	public String getMetaPath() {
		return metaPath;
	}

	public String getDataPath() {
		return dataPath;
	}

	public String[] getRouterServerList() {
		return routerServerList;
	}

	public int getMaxLoadSize() {
		return maxLoadSize;
	}

	public int getMaxWriteSize() {
		return maxWriteSize;
	}

	public String getCompressClassFullName() {
		return compressClassFullName;
	}

	public void setRouterServerList(String routerServerList) {
		this.routerServerList = routerServerList.split(",");
	}

	public void setMaxPostLen(int maxPostLen) {
		this.maxPostLen = maxPostLen;
	}

	public void setMetaPath(String metaPath) {
		this.metaPath = metaPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public void setMaxLoadSize(int maxLoadSize) {
		this.maxLoadSize = maxLoadSize;
	}

	public void setMaxWriteSize(int maxWriteSize) {
		this.maxWriteSize = maxWriteSize;
	}

	public void setReadQueueSize(int readQueueSize) {
		this.readQueueSize = readQueueSize;
	}

	public void setCompressClassFullName(String compressClassFullName) {
		this.compressClassFullName = compressClassFullName;
	}

	public int getRpcTimeOut() {
		return rpcTimeOut;
	}

	public void setRpcTimeOut(int rpcTimeOut) {
		this.rpcTimeOut = rpcTimeOut;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("routerServerList: ").append(this.routerServerList);
		sb.append(" maxPostLen: ").append(this.maxPostLen);
		sb.append(" metaPath: ").append(this.metaPath);
		sb.append(" dataPath: ").append(this.dataPath);
		sb.append(" maxWriteSize: ").append(this.maxWriteSize);
		sb.append(" maxLoadSize: ").append(this.maxLoadSize);
		sb.append(" readQueueSize: ").append(this.readQueueSize);
		sb.append(" appName: ").append(this.appName);
		sb.append(" compressClassFullName:").append(this.compressClassFullName);
		sb.append(" rpcTimeOut: ").append(this.rpcTimeOut);
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(Config.getInstance());
	}

	public int getReadQueueSize() {
		return readQueueSize;
	}

	public int getMaxPostLen() {
		return maxPostLen;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
}
