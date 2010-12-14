package com.taobao.timetunnel2.router.common;



public abstract class ParamsKey {
	public static class ZKService{
		public final static String hosts = "ZK_HOST_LIST";
		public final static String timeout = "ZK_TIMEOUT";
	}
	
	public static class ZKClient{
		public final static String size = "ZK_CLIENT_SIZE";
	}
	
	public static class ZNode{
		public final static String user = "/user";
		public final static String topic = "/categories";
		public final static String broker = "/brokers";
		public final static String session = "/clients";
	}
	
	public static class Service{
		public enum serverClass {
			NONBLOCK("com.taobao.timetunnel2.router.service.NonblockingRouterService"), 
			BLOCK("com.taobao.timetunnel2.router.service.PooledRouterService"), 
			TEST("com.taobao.timetunnel2.router.service.SampleRouterService");
			private String classname;
			serverClass(String classname){
				this.classname = classname;
			}
			public String getClassname(){
				return this.classname;
			}
		}
		
		public final static String serverType = "SERVER_TYPE";
		public final static String name = "SERVER_NAME";
		public final static String host = "SERVER_HOST";
		public final static String port = "SERVER_PORT";
		public final static String cliTimeout = "CLIENT_TIMEOUT";
		public final static String minThreads = "MIN_THREADS";
		public final static String maxThreads = "MAX_THREADS";
		public final static String stopTimeoutVal = "STOP_TIMEOUT_VAL";
		public final static String stopTimeoutUnit = "STOP_TIMEOUT_UNIT";
		public final static String maxReadBufferBytes = "MAX_READ_BUFFERBYTES";
		public final static String isPersisted = "IS_PERSISTED";

	}
	
	public static class LBPolicy{
		public final static String policy = "LB_POLICY";
		public final static String s_policy = "SEQ_LB_POLICY";
	}

	public static void main(String[] args) {
		try{
			System.out.println(ParamsKey.Service.serverClass.valueOf(null).getClassname());
		}catch(IllegalArgumentException e){
			e.printStackTrace();
		}
	}
}
