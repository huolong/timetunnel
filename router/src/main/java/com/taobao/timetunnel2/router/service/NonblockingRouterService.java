package com.taobao.timetunnel2.router.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TNonblockingServer.Options;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;

import com.taobao.timetunnel2.router.biz.BizRouterService;
import com.taobao.timetunnel.thrift.router.RouterService;
import com.taobao.timetunnel2.router.exception.ServiceException;
import com.taobao.timetunnel2.router.loadbalance.RouterContext;

public class NonblockingRouterService extends ServiceEngine{
	private static final Logger log = Logger.getLogger(NonblockingRouterService.class);
	private TNonblockingServer service;
	private RouterContext routercontext;
	
	@Override
	protected void doStart() throws ServiceException {
		try {		
			log.info("The nonblocking router server is in the process of starting...");
			routercontext = RouterContext.getContext();
			RouterService.Iface handler = new BizRouterService(routercontext);
			RouterService.Processor processor = new RouterService.Processor(handler);
			InetAddress iAddr = InetAddress.getByName(this.serviceProperties.getBindAddr());
			InetSocketAddress isAddr = new InetSocketAddress(iAddr,	this.serviceProperties.getPort());
			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(
					isAddr, this.serviceProperties.getCliTimeout());
			org.apache.thrift.server.TNonblockingServer.Options options = new Options();
			options.maxReadBufferBytes = this.serviceProperties.getMaxReadBufferBytes();
			TProcessorFactory processorFactory = new TProcessorFactory(processor);
			TFramedTransport.Factory outputTransportFactory = new TFramedTransport.Factory();
			TProtocolFactory inputProtocolFactory = new TBinaryProtocol.Factory();
			TProtocolFactory outputProtocolFactory = new TBinaryProtocol.Factory();
			service = new TNonblockingServer(processorFactory,serverTransport,
					outputTransportFactory,
					inputProtocolFactory, 
					outputProtocolFactory,
					options);			
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of starting " +
					"at the nonblocking router server."	+ e.getCause());
		}
	}
	
	@Override
	protected void doServe() throws ServiceException {
		log.info("The nonblocking router server has been started...");
		try{
			service.serve();
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of executing " +
					"at the nonblocking router server."	+ e.getCause());
		}
	}

	@Override
	protected void doStop() throws ServiceException {
		log.info("The nonblocking router server is in the process of stopping...");		
		try{
			service.stop();		
			routercontext.cleanup();
		} catch (Exception e) {
			throw new ServiceException(
					"There are some fatal errors in the process of stopping " +
					"at the nonblocking router server."	+ e.getCause());
		}
		log.info("The nonblocking router server has been stopped...");
	}
}
