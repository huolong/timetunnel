package com.taobao.timetunnel.client;

import java.nio.ByteBuffer;
import java.util.List;

import com.taobao.timetunnel.client.BufferFinance.Accountant;
import com.taobao.timetunnel.thrift.gen.ExternalService.Client;

/**
 * {@link Sub}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
final class Sub extends AClient {

  Sub(final ClientFactory factory,
      final String name,
      final int retry,
      final TestRuntimeReport report,
      final String category,
      final Accountant accountant) {
    super(factory, name, retry, report);
    this.category = category;
    this.accountant = accountant;
  }

  @Override
  protected int doCall(final Client client) throws Exception {
    final List<ByteBuffer> buffers = client.ackAndGet(category, token);
    for (final ByteBuffer buffer : buffers) {
      accountant.input(buffer);
    }
    return buffers.size();
  }

  @Override
  protected boolean running() {
    return accountant.balance();
  }

  private final Accountant accountant;
  private final String category;
}
