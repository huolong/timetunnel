package com.taobao.timetunnel2.router.loadbalance;

public abstract class Statistics implements Comparable<Statistics>{
	private String id;
	
	public Statistics(String id){
		setId(id);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public abstract void incRequest(int i);
	public abstract int getStatValue();	

	@Override
	public int compareTo(Statistics o) {
		return this.getStatValue() - ((Statistics) o).getStatValue();
	}
}
