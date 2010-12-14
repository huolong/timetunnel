package com.taobao.timetunnel.client;

import com.taobao.timetunnel.client.BufferFinance.Cashier;
import com.taobao.timetunnel.thrift.gen.ExternalService.Client;

/**
 * {@link Pub}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
final class Pub extends AClient {

  Pub(final ClientFactory factory,
      final String name,
      final int retry,
      final TestRuntimeReport report,
      final String category,
      final Cashier cashier) {
    super(factory, name, retry, report);
    this.cashier = cashier;
    this.category = category;
  }

  @Override
  protected int doCall(final Client client) throws Exception {
    client.post(category, token, cashier.output());
    return 1;
  }

  @Override
  protected boolean running() {
    return cashier.balance();
  }

  private final Cashier cashier;
  private final String category;
}
