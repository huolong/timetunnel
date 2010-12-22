package com.taobao.timetunnel2.router.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Options;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import com.taobao.timetunnel2.router.biz.BizRouterService;
import com.taobao.timetunnel.thrift.router.RouterService;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public class PooledRouterService extends ServiceEngine {
	private static final Logger log = Logger.getLogger(PooledRouterService.class);
	private TThreadPoolServer service;
	private RouterContext routercontext;
	
	@Override
	protected void doStart() throws ServiceException {
		try {
			log.info("The pooled router server is in the process of starting...");
			routercontext = RouterContext.getContext();

			RouterService.Iface handler = new BizRouterService(routercontext);
			RouterService.Processor processor = new RouterService.Processor(handler);

			InetAddress iAddr = InetAddress.getByName(this.serviceProperties.getBindAddr());
			InetSocketAddress isAddr = new InetSocketAddress(iAddr,	this.serviceProperties.getPort());
			TServerTransport serverTransport = new TServerSocket(isAddr,
					this.serviceProperties.getCliTimeout());

			Options options = new Options();
			options.maxWorkerThreads = this.serviceProperties.getMaxWorkerThreads();
			options.minWorkerThreads = this.serviceProperties.getMinWorkerThreads();
			options.stopTimeoutUnit = this.serviceProperties.getStopTimeoutUnit();
			options.stopTimeoutVal = this.serviceProperties.getStopTimeoutVal();

			service = new TThreadPoolServer(processor, serverTransport,
						new TFramedTransport.Factory(),
						new TFramedTransport.Factory(),
						new TBinaryProtocol.Factory(),
						new TBinaryProtocol.Factory(), options);
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of starting " +
					"at the pooled router server."	+ e.getCause());
		}
	}

	@Override
	protected void doServe() throws ServiceException {
		log.info("The pooled router server has been started...");
		try{
			service.serve();	
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of executing " +
					"at the pooled router server."	+ e.getCause());
		}
	}
	
	@Override
	protected void doStop() throws ServiceException {		
		log.info("The pooled router server is in the process of stopping...");	
		try{
			service.stop();		
			routercontext.cleanup();
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of stopping " +
					"at the pooled router server."+ e.getCause());
		}
		log.info("The pooled router server has been stopped...");
	}

}
