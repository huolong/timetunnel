package com.taobao.timetunnel.thrift.util;

import java.net.InetSocketAddress;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.THsHaServer.Options;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

/**
 * ServerUtils
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
public final class ServerUtils {
  private ServerUtils() {}

  public static final TServer newThriftServer(final String host,
                                              final int port,
                                              final int workThread,
                                              final long maxReadBufferBytes,
                                              final TProcessor processor) throws TTransportException {
    final TNonblockingServerSocket serverSocket =
      new TNonblockingServerSocket(new InetSocketAddress(host, port));
    final Options options = new Options();
    options.workerThreads = workThread;
    options.maxReadBufferBytes = maxReadBufferBytes;
    final THsHaServer tHsHaServer = new THsHaServer(processor, serverSocket, options);
    return tHsHaServer;
  }

}
