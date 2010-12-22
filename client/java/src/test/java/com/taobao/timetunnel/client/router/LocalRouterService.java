package com.taobao.timetunnel.client.router;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;

import com.taobao.timetunnel.client.util.SleepUtils;
import com.taobao.timetunnel.thrift.router.RouterService.Processor;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-8
 * 
 */
public class LocalRouterService {
	private static Logger log = Logger.getLogger(LocalRouterService.class);
	private static TServer server;
	private final static AtomicBoolean started = new AtomicBoolean(false);

	public static void bootstrap(int port, RouterImpl ri) {
		if (started.get()) {
			stop();
		}
		try {
			Processor processor = new Processor(ri);
			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
			THsHaServer.Options options = new THsHaServer.Options();
			options.workerThreads = 2;
			server = new THsHaServer(processor, serverTransport, options);
			new Thread() {
				public void run() {
					server.serve();
				}
			}.start();
			log.debug("Local router server started at port:" + port);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		started.set(true);
	}

	public static void openClose(int port, RouterImpl ri) {
		if (started.get())
			stop();
		else
			bootstrap(port, ri);
	}

	public static void stop() {
		server.stop();
		SleepUtils.sleep(1000);
		started.set(false);
	}

	public static void main(String[] args) {
		bootstrap(Integer.parseInt(args[0]), new RouterImpl());
	}

}
