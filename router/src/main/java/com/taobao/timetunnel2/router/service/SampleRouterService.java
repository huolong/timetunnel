package com.taobao.timetunnel2.router.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import com.taobao.timetunnel2.router.biz.BizRouterService;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel.thrift.router.RouterService;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public class SampleRouterService extends ServiceEngine {
	private static final Logger log = Logger.getLogger(SampleRouterService.class);
	private TServer service;
	private RouterContext routercontext;
	
	@Override
	protected void doStart() throws ServiceException {
		try {
			log.info("The test router server is in the process of starting...");
			routercontext = RouterContext.getContext();
			RouterService.Iface handler = new BizRouterService(routercontext);
			RouterService.Processor processor = new RouterService.Processor(handler);
			InetAddress iAddr = InetAddress.getByName(this.serviceProperties.getBindAddr());
			InetSocketAddress isAddr = new InetSocketAddress(iAddr,	this.serviceProperties.getPort());
			TServerTransport serverTransport = new TServerSocket(isAddr, this.serviceProperties.getCliTimeout());
			service = new TSimpleServer(processor, serverTransport);
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of starting " +
					"at the test router server." + e.getCause());
		}
	}
	
	@Override
	protected void doServe() throws ServiceException {
		log.info("The test router server is in the process of stopping...");	
		try{
			service.serve();
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of executing " +
					"at the test router server." + e.getCause());
		}
	}
	
	@Override
	protected void doStop() throws ServiceException {
		log.info("The test router server is in the process of stopping...");	
		try {
			service.stop();
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of stopping " + 
					"at the test router server." + e.getCause());
		}
		log.info("The test router server has been stopped...");
	}
}
