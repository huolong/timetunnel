package com.taobao.timetunnel2.router.biz;

public class BrokerUrl implements Comparable<BrokerUrl>{
	private String id;
	private String host;
	private String external;
	private String internal;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getExternal() {
		return external;
	}

	public void setExternal(String external) {
		this.external = external;
	}

	public String getInternal() {
		return internal;
	}

	public void setInternal(String internal) {
		this.internal = internal;
	}
	
	public String getExternalUrl(){
		return this.host+":"+this.external;		
	}
	
	public String getInternalUrl(){
		return this.host+":"+this.internal;		
	}
	
	@Override
	public int compareTo(BrokerUrl o) {		
		return this.id.compareTo(o.id);
	}
	


}
