package com.taobao.timetunnel2.router.loadbalance;

public class DefaultStatistics extends Statistics {
	private int requestcount;
	
	public DefaultStatistics(String id) {
		super(id);
		requestcount = 0;
	}
	
	public void incRequest(int i){
		requestcount+=i;
	}
	
	public int getRequestcount() {
		return requestcount;
	}

	@Override
	public int getStatValue() {
		return requestcount;
	}

}
