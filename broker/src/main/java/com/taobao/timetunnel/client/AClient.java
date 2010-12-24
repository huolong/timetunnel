package com.taobao.timetunnel.client;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import com.taobao.timetunnel.thrift.gen.ExternalService.Client;
import com.taobao.timetunnel.thrift.util.ClientUtils;
import com.taobao.util.Bytes;

/**
 * {@link AClient}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
abstract class AClient implements Callable<Void> {

  public AClient(final ClientFactory factory,
                 final String name,
                 final int retry,
                 final TestRuntimeReport report) {
    super();
    this.factory = factory;
    this.name = name;
    this.report = report;
    this.retry = retry;
    token = Bytes.toBuffer(name);
  }

  @Override
  public final Void call() throws Exception {
    final Client client = factory.create();
    while (running()) {
      final long begin = System.nanoTime();
      final int messages = tryDoCall(client);
      final long end = System.nanoTime();
      report.counterOfSuccess().increment();
      report.counterOfMessage().add(messages);
      report.hit(end - begin);
    }
    ClientUtils.close(client);
    return null;
  }

  @Override
  public final String toString() {
    return name;
  }

  protected abstract int doCall(Client client) throws Exception;

  protected abstract boolean running();

  private int tryDoCall(final Client client) throws Exception {
    for (int i = 0;;) {
      try {
        return doCall(client);
      } catch (final Exception e) {
        if (i++ < retry) report.counterOf(e).increment();
        else throw e;
      }
    }
  }

  protected final ByteBuffer token;

  private final int retry;
  private final String name;
  private final ClientFactory factory;;
  private final TestRuntimeReport report;

}
