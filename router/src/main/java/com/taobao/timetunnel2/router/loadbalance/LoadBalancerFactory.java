package com.taobao.timetunnel2.router.loadbalance;

import org.apache.log4j.Logger;

public class LoadBalancerFactory {	
	private final static Logger log = Logger.getLogger(LoadBalancerFactory.class);
	
	public static synchronized LoadBalancer getLoadBalancerPolicy(String policy){
		
		String pkg = LoadBalancerFactory.class.getPackage().getName();
		String className = pkg + "." + policy;
		LoadBalancer instance = null;
		try {
			instance =  (LoadBalancer) Class.forName(className).newInstance();
			return instance;
		} catch (InstantiationException e) {
			log.warn(String.format(
					"Creating the load balancer policy[%s] is failed.[%s]",
					className, e.getCause()));
		} catch (IllegalAccessException e) {
			log.warn(String.format(
					"Creating the load balancer policy[%s] is failed.[%s]",
					className, e.getCause()));
		} catch (ClassNotFoundException e) {
			log.warn(String.format(
					"Creating the load balancer policy[%s] is failed.[%s]",
					className, e.getCause()));
		}
		return getDefaultLoadBalancerPolicy();
	}
	
	private static LoadBalancer getDefaultLoadBalancerPolicy(){
		return new RoundRobinLoadBalancer();
	}
	
}
