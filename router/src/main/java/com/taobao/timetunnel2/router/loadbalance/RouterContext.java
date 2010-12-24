package com.taobao.timetunnel2.router.loadbalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.data.Stat;

import com.taobao.timetunnel.thrift.router.Constants;
import com.taobao.timetunnel2.router.biz.BrokerUrl;
import com.taobao.timetunnel2.router.biz.UserInfo;
import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.common.Util;
import com.taobao.timetunnel2.router.common.ValidationException;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.service.ServiceEngine;
import com.taobao.timetunnel2.router.zkclient.Visitor;
import com.taobao.timetunnel2.router.zkclient.ZooKeeperClientPool;
import com.taobao.timetunnel2.router.zkclient.ZooKeeperMonitor;
import com.taobao.timetunnel2.router.zkclient.ZooKeeperMonitor.WatchType;
import com.taobao.timetunnel2.router.zkclient.ZookeeperProperties;
import com.taobao.timetunnel2.router.zkclient.ZookeeperService;
import com.taobao.timetunnel2.router.zkclient.ZookeeperServiceAgent;

public class RouterContext implements Context, Visitor{
	private static final Logger log = Logger.getLogger(RouterContext.class);
	private Properties appParam;
	private LoadBalancer policy;
	private LoadBalancer s_policy;
	private ZooKeeperMonitor monitor;
	private static RouterContext context;
	private ConcurrentHashMap<String, String> authMap = new ConcurrentHashMap<String, String>(); 	
	private final AtomicBoolean synced = new AtomicBoolean(false);
	private RouterMap routerMap = RouterMap.getInstance();
	private ZooKeeperClientPool zkpool = ZooKeeperClientPool.getInstance();	
	private RouterContext() throws ServiceException {	
		init();
	}

	public static RouterContext getContext() throws ServiceException {
		if (context == null)
			context = new RouterContext();
		return context;
	}
	
	private void init() throws ServiceException{
		//loadConf();
		appParam = Util.loadConf();
		Map<String, WatchType> watchpaths = new HashMap<String, WatchType>();
		watchpaths.put(ParamsKey.ZNode.topic, WatchType.DataChanged);
		watchpaths.put(ParamsKey.ZNode.user, WatchType.DataChanged);
		watchpaths.put(ParamsKey.ZNode.broker, WatchType.ChildrenChanged);
		ZookeeperProperties zprop = new ZookeeperProperties(appParam);
		monitor = new ZookeeperServiceAgent(zprop, watchpaths, this);
		s_policy = LoadBalancerFactory.getLoadBalancerPolicy(appParam
				.getProperty(ParamsKey.LBPolicy.s_policy, "ConstantLoadBalancer"));
		policy = LoadBalancerFactory.getLoadBalancerPolicy(appParam
				.getProperty(ParamsKey.LBPolicy.policy,	"RoundRobinStatelessLoadBalancer"));
		monitor.doServe();
		sync();
	}
	
/*	private void loadConf() throws ServiceException{
		appParam = new Properties();
		try {
			appParam.load(this.getClass().getClassLoader().getResourceAsStream(RouterConsts.ROUTER_PATH));
		} catch (FileNotFoundException e) {
			throw new ServiceException(String.format(
					"The router-config file does not exist.[%s]",
					RouterConsts.ROUTER_PATH));
		} catch (IOException e) {
			log.error(e);
			throw new ServiceException(String.format(
					"There are some error in reading from the router config file.[%s]",
					e.getCause()));
		}
	}*/

	@Override
	public void sync(){
		boolean exitFlag = false;
		try {
			ServiceEngine srv = ServiceEngine.getInstance();
			exitFlag = !srv.isStarted(); 			
		} catch (ServiceException e) {
		}	
		syncTopic(exitFlag);
		syncAuthInfo(exitFlag);		
	}
	
	public List<String> getSessionStats(String topic){		
		return routerMap.getRouters(topic);
	}
	
	public String authenticate(String userId, String password, String topic,
			Map<String, String> prop) throws ServiceException {
		String pwd = authMap.get(userId);
		String sessionId = null;		
		if ((password == null && pwd == null) || (password!=null && password.equals(pwd)))
			sessionId = generateToken(userId,password,topic,prop);
		return sessionId;
	}
	
	private String generateToken(String userId, String password, String topic,
			Map<String, String> prop) throws ServiceException {
		String token = "";
		try{
			String clientId = Util.getStrParam(Constants.LOCAL_HOST, prop.get(Constants.LOCAL_HOST))+RouterConsts.ID_SPLIT+topic;
			String clientType = Util.getStrParam(Constants.TYPE, prop.get(Constants.TYPE));
			String timeout = Util.getStrParam(Constants.TIMEOUT, prop.get(Constants.TIMEOUT));
			String prefix = "";
			if(Boolean.parseBoolean(appParam.getProperty(ParamsKey.Service.isPersisted, "true"))){
				try{
					StringBuilder json = new StringBuilder(512);
					json.append("{\"type\":\"").append(clientType)
						.append("\", \"timeout\":\"").append(timeout);
					
					if ("SUB".equalsIgnoreCase(clientType)){
						String size = Util.getStrParam(Constants.RECVWINSIZE, prop.get(Constants.RECVWINSIZE));
						json.append("\", \"subscriber\":\"").append(topic+"-"+userId)
						    .append("\", \"receiveWindowSize\":\"").append(size);
						prefix="s"; 					
					}else
					{
						prefix="p"; 	
					}
					json.append("\"}");
					ZookeeperService zks = zkpool.getZooKeeperClient();
					List<String> dirs = zks.getChildren(ParamsKey.ZNode.session+"/"+clientId);
					/*
					CountDownLatch count = new CountDownLatch(1);
					CCallback ccb = new CCallback();		
					zks.getChildren(ParamsKey.ZNode.session+"/"+clientId, ccb, count);
					try {
						count.await(2, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						return null;
					}
					List<String> dirs = ccb.getResult();*/
					if(dirs!=null && dirs.size()>0){	
						for(String path: dirs){						
							if(path.startsWith("s") && "SUB".equals(clientType) ||
							   path.startsWith("p") && "PUB".equals(clientType) ){
								return ParamsKey.ZNode.session+"/"+clientId+"/"+path;
							}
						}
					}
					/*SetDataCallBack cb = new SetDataCallBack();
					CountDownLatch signal = new CountDownLatch(1);*/
					String sessionId = Util.getMD5(String.valueOf(System.nanoTime())+clientId);
					token = ParamsKey.ZNode.session+"/"+clientId+"/"+prefix+sessionId;
					zks.setData(token, json.toString());
					/*try {
						signal.await(2, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						return null;
					}*/
									
				}catch(ValidationException e){
					throw new ServiceException(e);				
				}catch (Exception e) {
					throw new ServiceException(
							"Authentication failure: Zookeeper service is unavailable..." +
							"Please retry a few minutes later.. ");
				}
			}
		}catch(ValidationException e){
			throw new ServiceException(e);
		}
		return token;
	}
	
	public LoadBalancer getPolicy(String mode) {	
		if(ParamsKey.LBPolicy.s_policy.equals(mode))
			return s_policy;
		else
			return policy;
	}

	public void setPolicy(String mode, LoadBalancer lbpolicy) {
		if(ParamsKey.LBPolicy.s_policy.equals(mode))
			this.s_policy = lbpolicy;
		else
			this.policy = lbpolicy;
	}
	
	public Properties getAppParam() {
		return appParam;
	}

	@Override
	public void cleanup() {
		if(monitor!=null)
			monitor.finish();		
		authMap = null;
		routerMap = null;
		if(zkpool!=null)
			zkpool.close();
		context = null;
	}
	
	class SetDataCallBack implements StatCallback{
		@Override
		public void processResult(int rc, String path, Object ctx, Stat stat) {
			CountDownLatch signal = (CountDownLatch)ctx;
			signal.countDown();
		}
		
	}
	
	class CCallback implements ChildrenCallback{
		private List<String> dirs; 
		
		@Override
		public void processResult(int rc, String path, Object ctx,
				List<String> dirs) {			
			this.dirs = dirs;
			CountDownLatch count = (CountDownLatch)ctx;
			count.countDown();
		}	
		
		public List<String> getResult(){
			return dirs;
		}
	}
	
	public void syncTopic(boolean exitFlag){	
		if (synced.compareAndSet(false, true)) {
			try {
				ZookeeperService zks = zkpool.getZooKeeperClient();
				List<String> topics = null;
				try {
					topics = zks.getChildren(ParamsKey.ZNode.topic);
				} catch (Exception e) {
					log.error(e.toString());
					if (exitFlag)
						System.exit(-1);
				}
				routerMap.clear();
				if (topics != null && topics.size() > 0) {
					Set<String> newBrokers = new HashSet<String>();
					for (String topic : topics) {
						String grptemp = zks.getData(ParamsKey.ZNode.topic + "/" + topic);
						try{						
							if(grptemp!=null){							
								String group = grptemp.substring(
										grptemp.indexOf("\"group\":") + 9,
										grptemp.indexOf("}") - 1);
								List<String> clusters = zks.getChildren(ParamsKey.ZNode.broker + "/"+ group);
								if(clusters!=null && clusters.size()>0){
									List<BrokerUrl> srvlists = new ArrayList<BrokerUrl>();
									for (String srvname : clusters) {
									try {
										String brokerurl = zks.getData(ParamsKey.ZNode.broker + "/"
														+ group + "/" + srvname);
										BrokerUrl broker = (BrokerUrl) Util.fromJson(
												brokerurl, BrokerUrl.class);
										broker.setId(srvname);
										srvlists.add(broker);
										newBrokers.add(srvname);
										log.info(String.format("topic=%s,srvname=%s", topic, srvname));
									} catch (Exception e) {
										log.error(String.format("topic=%s,srvname=%s,%s", topic, srvname, e.toString()));
									}
								}
								if (srvlists.size() > 0)
									routerMap.update(topic, srvlists);
								}								
							}
						} catch (Exception e) {
							log.error("topic[" + topic +"/" + grptemp + "]:" + e.toString());
						}
					}
					routerMap.changeClientStatus(newBrokers);
				} else {
					log.error("The router Server doesn't provide available service because of no topic znode at \""
							+ ParamsKey.ZNode.topic + "\".");									
					if (exitFlag)
						System.exit(-1);					
				}
			} catch (Exception e) {
				log.error(e.toString());				
			} finally {
				synced.set(false);
			}
		}
	}
	
	private void syncAuthInfo(boolean exitFlag){		
		if (synced.compareAndSet(false, true)) {
			try {				
				ZookeeperService zks = zkpool.getZooKeeperClient();
				List<String> users = zks.getChildren(ParamsKey.ZNode.user);					
				authMap.clear();
				if (users != null && users.size() > 0) {							
					for (String user : users) {
						try {
							String authstr = zks.getData(ParamsKey.ZNode.user + "/" + user);
							UserInfo userinfo = (UserInfo) Util.fromJson(
									authstr, UserInfo.class);
							authMap.put(user, userinfo.getPassword());
						} catch (Exception e) {
							log.error(e.toString());
							if (exitFlag)
								System.exit(-1);
						}
					}
				}else{
					log.error("The router Server doesn't provide available service because of no user znode at \""
							+ ParamsKey.ZNode.user + "\".");
					if (exitFlag)
						System.exit(-1);
				}
			} catch (Exception e) {
				log.error(e.toString());
				if (exitFlag)
					System.exit(-1);
			} finally {
				synced.set(false);
			}
		}
	}

	@Override
	public void onNodeChildrenChanged(String path, List<String> children) {
		syncTopic(false);	
	}

	@Override
	public void onNodeCreated(String path) {		
		if(path!=null){
			if(ParamsKey.ZNode.user.equals(path))
				syncAuthInfo(false);	
			else
				syncTopic(false);
		}
	}

	@Override
	public void onNodeDataChanged(String path) {
		if(path!=null){
			 if(ParamsKey.ZNode.user.equals(path))
				 syncAuthInfo(false);	
			 else
				 syncTopic(false);	
		}
	}

	@Override
	public void onNodeDeleted(String path) {
		if(path!=null){
			if(ParamsKey.ZNode.user.equals(path))
				syncAuthInfo(false);	
			else
				syncTopic(false);
		}
		
	}

}
