package com.taobao.timetunnel.thrift.util;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * ClientUtils
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-9
 * 
 */
public class ClientUtils {
  private ClientUtils() {}

  public static final void close(final TServiceClient client) {
    client.getInputProtocol().getTransport().close();
    client.getOutputProtocol().getTransport().close();
  }

  public static final <T extends TServiceClient> T newClient(TServiceClientFactory<T> factory,
                                                             final String host,
                                                             final int port) throws TTransportException {
    final TTransport transport = new TFramedTransport(new TSocket(host, port));
    transport.open();
    return factory.getClient(new TBinaryProtocol(transport));
  }

  public static final <T extends TServiceClient> T newClient(TServiceClientFactory<T> factory,
                                                             final InetSocketAddress address) throws TTransportException,
                                                                                             IOException {
    final TTransport transport =
      new TFramedTransport(new TSocket(address.getHostName(), address.getPort()));
    transport.open();
    return factory.getClient(new TBinaryProtocol(transport));
  }
}
