package com.taobao.timetunnel.client;

import static com.taobao.timetunnel.thrift.util.ClientUtils.newClient;

import org.apache.thrift.transport.TTransportException;

import com.taobao.timetunnel.thrift.gen.ExternalService.Client;
import com.taobao.timetunnel.thrift.gen.ExternalService.Client.Factory;

/**
 * {@link ClientFactory}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-8
 * 
 */
public final class ClientFactory {

  public Client create() throws TTransportException {
    return newClient(new Factory(), host, port);
  }

  public ClientFactory(String host, int port) {
    super();
    this.host = host;
    this.port = port;
  }

  private final String host;
  private final int port;
}
