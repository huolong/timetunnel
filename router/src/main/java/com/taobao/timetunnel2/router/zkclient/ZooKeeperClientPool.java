package com.taobao.timetunnel2.router.zkclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.taobao.timetunnel2.router.common.RouterConsts;

public class ZooKeeperClientPool {
	private List<ZookeeperService> zkpool;
	private int size=1;
	private int counter=-1;
	private static final ZooKeeperClientPool instance = new ZooKeeperClientPool();

	public void setPoolSize(int size){
		this.size = size;
	}
	
	public static ZooKeeperClientPool getInstance(){
		return instance;
	}
	
	private ZooKeeperClientPool(){
		Properties prop = new Properties();
		try {
			prop.load(this.getClass().getClassLoader().getResourceAsStream(RouterConsts.ROUTER_PATH));			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ZookeeperProperties zkprop = new ZookeeperProperties(prop);
		size = zkprop.getPoolSize();
		zkpool = new ArrayList<ZookeeperService>(size);
		for(int i=0; i<size; i++){
			ZookeeperService zookeeper = new ZooKeeperExector(zkprop); 
			zkpool.add(zookeeper);			
		}
	}
	
	public synchronized ZookeeperService getZooKeeperClient(){
		if(size==1)
			return zkpool.get(0);
		if (++counter >= size) {
            counter = 0;
        }
        return zkpool.get(counter);
	}
	
	public void close(){
		for(int i=0; i<size; i++){
			ZookeeperService zookeeper = zkpool.get(i);	
			if(zookeeper!=null){
				zookeeper.close();
			}
		}
	}

}
