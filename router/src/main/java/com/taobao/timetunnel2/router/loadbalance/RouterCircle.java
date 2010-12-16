package com.taobao.timetunnel2.router.loadbalance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.timetunnel2.router.biz.BrokerUrl;

public class RouterCircle {
	private Map<String, RouterNode> circle;	
	private String topic;	
	private String cursor;
	private int startIndex;	
	private int count;
	
	public RouterCircle(String topic){
		this(topic, 0);
	}
	
	public RouterCircle(String topic, int startIndex){
		this.topic = topic;
		this.startIndex = startIndex;
		this.count = 0;
	}
	
	public String getCursor() {
		return cursor;
	}
	
	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
	
	public String getTopic() {
		return topic;
	}	

	public int getCount() {
		return count;
	}
	
	public void next(){
		if(cursor!=null){
			RouterNode node = circle.get(cursor);	
			if(node!=null)
				cursor = node.getFollower();
		}
	}
	
	public String getCurrentNode(){
		RouterNode node = circle.get(cursor);
		return node.getNode();
	}
	
	public String getCurrentNodeAndNext(){
		RouterNode node = circle.get(cursor);
		cursor = node.getFollower();
		return node.getNode();
	}
	
	public String getFollowerNode(){
		RouterNode node = circle.get(cursor);
		cursor = node.getFollower();
		return cursor;
	}

	public String getFollowerNode(String pointer){
		RouterNode node = circle.get(pointer);
		if(node==null)
			return null;
		return node.getFollower();
	}

	public Map<String, RouterNode> createCircleRouter(List<BrokerUrl> brokers){
		if (brokers==null )
			return null;
		count=brokers.size();
		if (count==0){
			return null;
		}
		if(circle==null){
			circle = new HashMap<String, RouterNode>();
		}
		Collections.sort(brokers);
		if (count>0){			
			for (int i = 0; i < count; i++) {
				RouterNode node = new RouterNode();
				
				node.setNode(brokers.get(i).getExternalUrl());
				if(startIndex==i)
					cursor = node.getNode();
				if (i==0){	
					node.setLeader(brokers.get(count-1).getExternalUrl());
					if (count==1){						
						node.setFollower(brokers.get(0).getExternalUrl());
					}else{						
						node.setFollower(brokers.get(i+1).getExternalUrl());
					}	
					
				}else if (i>0 && i<count-1){
					node.setLeader(brokers.get(i-1).getExternalUrl());					
					node.setFollower(brokers.get(i+1).getExternalUrl());
				}else{
					node.setLeader(brokers.get(i-1).getExternalUrl());					
					node.setFollower(brokers.get(0).getExternalUrl());
				}
				circle.put(node.getNode(),node);
			}
		}
		return circle;
	}
	
	public List<String> getNodes(){
		List<String> nodes = new ArrayList<String>(circle.keySet());
		return nodes;
	}
	
}
