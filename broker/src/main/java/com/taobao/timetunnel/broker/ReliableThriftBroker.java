package com.taobao.timetunnel.broker;

import static com.taobao.timetunnel.thrift.util.ServerUtils.newThriftServer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.Appendable;
import com.taobao.timetunnel.broker.ReliableServiceClients.ReliableServiceClient;
import com.taobao.timetunnel.center.Center;
import com.taobao.timetunnel.message.ByteBufferMessageCompactor;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.message.MessageFactory;
import com.taobao.timetunnel.reliable.ByteBufferMessageReliables;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.thrift.gen.ExternalService;
import com.taobao.timetunnel.thrift.gen.Failure;
import com.taobao.timetunnel.thrift.gen.InternalService;
import com.taobao.timetunnel.tunnel.ByteBufferMessageTunnels;
import com.taobao.timetunnel.tunnel.TrimListener;
import com.taobao.util.DirectoryCleaner;
import com.taobao.util.JsonUtils;
import com.taobao.util.MemoryMonitor;

/**
 * {@link ReliableThriftBroker}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-17
 * 
 */
public final class ReliableThriftBroker extends ThriftBroker<ByteBuffer> implements TrimListener {

  public ReliableThriftBroker(final Center center,
                              final String host,
                              final int external,
                              final int internal,
                              final String group,
                              final int syncPoint,
                              final int maxMessageSize,
                              final MemoryMonitor monitor,
                              final File home) {
    super(center, group);
    this.maxMessageSize = maxMessageSize;
    info = new Info(host, external, internal);

    DirectoryCleaner.clean(home);
    LOGGER.info("clean directory {}.", home);

    final MessageFactory<ByteBuffer> messageFactory =
      new ByteBufferMessageCompactor(monitor, new File(home, "tunnels"), maxMessageSize);
    tunnels = new ByteBufferMessageTunnels(this, syncPoint, messageFactory);
    reliables = new ByteBufferMessageReliables(new File(home, "reliables"), maxMessageSize);
  }

  @Override
  public void onTrim(final Category category, final Session session, final int size) {
    try {
      reliableService.get().trim(category, session, size);
    } catch (final RuntimeException e) {
      LOGGER.error("Unexpected error on trimming.", e);
    }
  }

  @Override
  protected void doStart() {
    try {
      internal =
        newThriftServer(info.host,
                        info.internal,
                        workThread,
                        maxReadBufferBytes,
                        internalProcessror());
      external =
        newThriftServer(info.host,
                        info.external,
                        workThread,
                        maxReadBufferBytes,
                        externalProcessror());
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

    startInternal();

    doExternalServe();

    stopInternal();

    disposeReliableServiceClient();
  }

  @Override
  protected void doStop() {
    if (external != null) external.stop();
    tunnels.dispose();
    reliables.dispose();
  }

  @Override
  protected String info() {
    return JsonUtils.json(info);
  }

  @Override
  protected void onClusterChanged(final List<String> newCluster) {
    // if (newCluster.size() < 2) return;
    try {
      if (neighborChanged(newCluster) && followIsNotSelf()) {
        LOGGER.info("Broker {} begin dumping to {}.", id(), follow);
        tunnels.dumpTo(new Appendable<ByteBuffer>() {

          @Override
          public void append(final Category category,
                             final Session session,
                             final ByteBuffer message) {
            reliableService.get().dump(category, session, message);
          }
        });
        LOGGER.info("Broker {} end dumping to {}.", id(), follow);
      }
    } catch (final RuntimeException e) {
      LOGGER.error("Unexpected error on cluster changed.", e);
    }
  }

  @Override
  protected void onLossContactWithCluster() {
    reregister();
    LOGGER.info("Broker reregistered.");
  }

  private void changeReliableServiceTo(final String follow) {
    final ReliableServiceClient newClient =
      (followIsNotSelf()) ? newReliableServiceClient(follow) : ReliableServiceClients.NONE;
    final ReliableServiceClient oldClient = reliableService.getAndSet(newClient);
    if (oldClient != null) oldClient.dispose();
  }

  private void disposeReliableServiceClient() {
    final ReliableServiceClient client = reliableService.get();
    if (client != null) {
      client.dispose();
      LOGGER.info("ReliableServiceClient disposed.");
    }
  }

  private void doExternalServe() {
    Thread.currentThread().setName("broker-external");
    external.serve();
  }

  private TProcessor externalProcessror() {
    return new ExternalService.Processor(new External());
  }

  private boolean followChangedIn(final List<String> newCluster, final int i) {
    final int index = i + 1 == newCluster.size() ? 0 : i + 1;
    final boolean changed = !newCluster.get(index).equals(follow);
    if (changed) follow = newCluster.get(index);
    return changed;
  }

  private boolean followIsNotSelf() {
    return !id().equals(follow);
  }

  private TProcessor internalProcessror() {
    return new InternalService.Processor(new Internal());
  }

  private boolean leadChangedIn(final List<String> newCluster, final int i) {
    final int index = i - 1 < 0 ? newCluster.size() - 1 : i - 1;
    final boolean changed = !newCluster.get(index).equals(lead);
    if (changed) lead = newCluster.get(index);
    return changed;
  }

  private void mergeReliablesToTunnels() {
    LOGGER.info("Broker {} begin merging.", id());
    reliables.dumpTo(new Appendable<ByteBuffer>() {

      @Override
      public void append(final Category category, final Session session, final ByteBuffer message) {
        tunnels.tunnel(category).post(session, message);
      }
    });
    LOGGER.info("Broker {} end merging.", id());
  }

  private boolean neighborChanged(final List<String> newCluster) {
    Collections.sort(newCluster);
    boolean changed = false;
    for (int i = 0; i < newCluster.size(); i++) {
      if (newCluster.get(i).equals(id())) {

        if (leadChangedIn(newCluster, i)) {
          mergeReliablesToTunnels();
          changed = true;
          LOGGER.info("Broker {} change lead to {}.", id(), lead);
        }

        if (followChangedIn(newCluster, i)) {
          changeReliableServiceTo(follow);
          changed = true;
          LOGGER.info("Broker {} change follow to {}.", id(), follow);
        }

        break;
      }
    }
    return changed;
  }

  private ReliableServiceClient newReliableServiceClient(final String follow) {
    try {
      return ReliableServiceClients.newClient(reliableServiceAddress(follow));
    } catch (final TTransportException e) {
      LOGGER.error("Can't new a reliable service client, broker still is working but not reliable.",
                   e);
      return ReliableServiceClients.NONE;
    }
  }

  private void startInternal() {
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          internal.serve();
        } catch (final Exception e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    }, "broker-internal").start();
  }

  private void stopInternal() {
    if (internal != null) {
      internal.stop();
      LOGGER.info("Internal Server disposed.");
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ReliableThriftBroker.class);

  private final AtomicReference<ReliableServiceClient> reliableService =
    new AtomicReference<ReliableServiceClient>(ReliableServiceClients.NONE);

  private final ByteBufferMessageTunnels tunnels;
  private final ByteBufferMessageReliables reliables;
  private final int maxMessageSize;
  private final Info info;

  private volatile String lead = "";
  private volatile String follow = "";

  private TServer internal;
  private TServer external;

  /**
   * {@link External}
   */
  private final class External implements ExternalService.Iface {

    @Override
    public List<ByteBuffer> ackAndGet(final String category, final ByteBuffer token) throws Failure,
                                                                                    TException {
      try {
        final List<ByteBuffer> gets = tunnels.tunnel(category(category)).ackAndGet(session(token));
        LOGGER.debug("AckAndGet {} message of {} from {}", new Object[] { gets.size(),
          category(category), session(token) });
        return gets;
      } catch (final RuntimeException e) {
        LOGGER.error("Broker ackAndGet failed.", e);
        throw failure(e);
      }
    }

    @Override
    public void post(final String category, final ByteBuffer token, final ByteBuffer message) throws Failure,
                                                                                             TException {
      try {
        if (message.remaining() > maxMessageSize)
          throw new TooBigMessageException(message.remaining() + " > " + maxMessageSize);

        reliableService.get().copy(category(category), session(token), message);
        tunnels.tunnel(category(category)).post(session(token), message);
        LOGGER.debug("Post a message of {} from {}", category(category), session(token));
      } catch (final RuntimeException e) {
        LOGGER.error("Broker post failed.", e);
        throw failure(e);
      }
    }

  }

  /**
   * {@link Info}
   */
  private final static class Info {

    public Info(final String host, final int external, final int internal) {
      super();
      this.host = host;
      this.external = external;
      this.internal = internal;
    }

    private final int external;
    private final int internal;
    private final String host;
  }

  /**
   * {@link Internal}
   */
  private final class Internal implements InternalService.Iface {

    @Override
    public void copy(final String category, final ByteBuffer token, final ByteBuffer message) throws Failure,
                                                                                             TException {
      try {
        reliables.reliable(category(category)).copy(session(token), message);
        LOGGER.debug("Copy a message of {} from {}", category(category), session(token));
      } catch (final RuntimeException e) {
        LOGGER.error("Broker copy failed.", e);
        throw failure(e);
      }
    }

    @Override
    public void dump(final String category, final ByteBuffer token, final ByteBuffer message) throws Failure,
                                                                                             TException {
      try {
        reliables.reliable(category(category)).dump(session(token), message);
        LOGGER.debug("Dump a message of {} from {}", category(category), session(token));
      } catch (final RuntimeException e) {
        LOGGER.error("Broker dump failed.", e);
        throw failure(e);
      }
    }

    @Override
    public void trim(final String category, final ByteBuffer token, final int size) throws Failure,
                                                                                   TException {
      try {
        reliables.reliable(category(category)).trim(session(token), size);
        LOGGER.debug("Trim {} message of {} from {}", new Object[] { size, category(category),
          session(token) });
      } catch (final RuntimeException e) {
        LOGGER.error("Broker trim failed.", e);
        throw failure(e);
      }
    }

  }

}
