package com.taobao.timetunnel2.router.loadbalance;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.data.Stat;

import com.taobao.timetunnel.thrift.router.Constants;
import com.taobao.timetunnel2.router.common.ParamsKey;
import com.taobao.timetunnel2.router.common.RouterConsts;
import com.taobao.timetunnel2.router.common.Util;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.zkclient.AsyncZookeeperClient;
import com.taobao.timetunnel2.router.zkclient.ZooKeeperClientPool;
import com.taobao.timetunnel2.router.zkclient.ZookeeperProperties;
import com.taobao.timetunnel2.router.zkclient.ZookeeperService;
import com.taobao.timetunnel2.router.zkclient.ZookeeperServiceAgent;

public class RouterContext implements Context{
	private static final Logger log = Logger.getLogger(RouterContext.class);
	private Properties appParam;
	private LoadBalancer policy;
	private LoadBalancer s_policy;
	private ZookeeperService zks;
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
		loadConf();
		Map<String, String> watchpaths = new HashMap<String, String>();
		watchpaths.put(ParamsKey.ZNode.topic, RouterConsts.WATCH_MODE_SETDATA);
		watchpaths.put(ParamsKey.ZNode.user, RouterConsts.WATCH_MODE_SETDATA);
		watchpaths.put(ParamsKey.ZNode.broker, RouterConsts.WATCH_MODE_CHILDCHANGE);
		/*String[] watchpaths = {ParamsKey.ZNode.topic, ParamsKey.ZNode.user, ParamsKey.ZNode.broker};*/
		zks = new ZookeeperServiceAgent(new ZookeeperProperties(appParam),
				this, watchpaths);
		s_policy = LoadBalancerFactory.getLoadBalancerPolicy(appParam
				.getProperty(ParamsKey.LBPolicy.s_policy,	"ConstantLoadBalancer"));
		policy = LoadBalancerFactory.getLoadBalancerPolicy(appParam
				.getProperty(ParamsKey.LBPolicy.policy,	"RoundRobinStatelessLoadBalancer"));

		zks.doServe();
		sync();
	}
	
	private void loadConf() throws ServiceException{
		appParam = new Properties();
		try {
			//appParam.load(new FileInputStream( new File(RouterConsts.ROUTER_PATH)));
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
	}

	@Override
	public void sync(){
		if (synced.compareAndSet(false, true)) {
			try {
				List<String> topics = null;
				try {
					topics = zks.getChildren(ParamsKey.ZNode.topic);
				} catch (Exception e) {
					log.error(e.toString());
				}
				routerMap.clear();				
				if (topics != null && topics.size() > 0) {					
					Set<String> newBrokers = new HashSet<String>();
					for (String topic : topics) {
						try {
							String grptemp = zks.getData(ParamsKey.ZNode.topic + "/" + topic);
							String group = grptemp.substring(
									grptemp.indexOf("\"group\":") + 9,
									grptemp.indexOf("}")-1);
							List<String> clusters = zks.getChildren(
									ParamsKey.ZNode.broker + "/" + group);
							List<BrokerUrl> srvlists = new ArrayList<BrokerUrl>();
							for (String srvname : clusters) {
								try {
									String brokerurl = zks.getData(ParamsKey.ZNode.broker + "/" + group + "/"
											+ srvname);
									BrokerUrl broker = (BrokerUrl) Util.fromJson(brokerurl, BrokerUrl.class);
									broker.setId(srvname);
									srvlists.add(broker);
									newBrokers.add(srvname);
									log.info(String.format(
											"topic=%s,srvname=%s", topic, srvname));
								} catch (Exception e) {
									e.printStackTrace();
									log.error(e.toString());
								}
							}
							if (srvlists.size() > 0)								
								routerMap.update(topic, srvlists);
						} catch (Exception e) {
							log.error("topic[" + topic + "]:" + e.toString());
						}
					}
					routerMap.changeClientStatus(newBrokers);					
				}
				try {
					List<String> users = zks.getChildren(ParamsKey.ZNode.user);
					authMap.clear();
					if (users != null && users.size() > 0) {							
						for (String user : users) {
							try {
								authMap.put(user,
										zks.getData(ParamsKey.ZNode.user + "/" + user));
							} catch (Exception e) {
								log.error(e.toString());
							}
						}
					}
				} catch (Exception e) {
					log.error(e.toString());
				}

			} catch (Exception e) {
				log.error(e.toString());
			} finally {
				synced.set(false);
			}
		}
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
		String clientId = prop.get(Constants.LOCAL_HOST)+RouterConsts.ID_SPLIT+topic;
		String clientType = prop.get(Constants.TYPE);
		String timeout = prop.get(Constants.TIMEOUT);
		String size = prop.get(Constants.RECVWINSIZE);
		long timestamp = System.nanoTime();

		String sessionId = Util.getMD5(String.valueOf(timestamp)+clientId);
		String token = "";

		if(Boolean.parseBoolean(appParam.getProperty(ParamsKey.Service.isPersisted, "true"))){
			try{
				StringBuilder json = new StringBuilder(512);
				json.append("{\"type\":\"").append(clientType)
					.append("\", \"timeout\":\"").append(timeout)
					.append("\", \"token\":\"").append(sessionId);
					
				if ("SUB".equalsIgnoreCase(clientType))
					json.append("\", \"subscriber\":\"").append(userId)
					    .append("\", \"receiveWindowSize\":\"").append(size);
				
				json.append("\"}");
				AsyncZookeeperClient zkclient = zkpool.getZooKeeperClient();
				CountDownLatch count = new CountDownLatch(1);
				CCallback ccb = new CCallback();
				zkclient.getChildren(ParamsKey.ZNode.session+"/"+clientId, ccb, count);
				try {
					count.await(2, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					return null;
				}
				List<String> dirs = ccb.getResult();
				if(dirs!=null && dirs.size()>0){
					token = ParamsKey.ZNode.session+"/"+clientId+"/"+dirs.get(0);
				}else{
					SetDataCallBack cb = new SetDataCallBack();
					CountDownLatch signal = new CountDownLatch(1);
					token = ParamsKey.ZNode.session+"/"+clientId+"/"+sessionId;
					zkclient.setData(token, json.toString(), cb, signal);
					try {
						signal.await(2, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						return null;
					}
				}				
			}catch (Exception e) {
				throw new ServiceException("Authentication failure: Zookeeper service is unavailable...Please retry a few minutes later.. ");
			}
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
		if(zks!=null)
			zks.finish();		
		authMap = null;
		routerMap = null;
		if(zkpool!=null)
			zkpool.close();
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
	
}
