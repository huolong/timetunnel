package com.taobao.timetunnel2.router.loadbalance;

public class RouterNode {
	private String node;
	private String leader;	
	private String follower;
	
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getLeader() {
		return leader;
	}
	public void setLeader(String leader) {
		this.leader = leader;
	}
	public String getFollower() {
		return follower;
	}
	public void setFollower(String follower) {
		this.follower = follower;
	}
}
