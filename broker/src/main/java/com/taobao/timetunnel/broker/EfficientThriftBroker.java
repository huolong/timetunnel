package com.taobao.timetunnel.broker;

import static com.taobao.timetunnel.thrift.util.ServerUtils.newThriftServer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.center.Center;
import com.taobao.timetunnel.message.ByteBufferMessageCompactor;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.message.MessageFactory;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.thrift.gen.ExternalService;
import com.taobao.timetunnel.thrift.gen.ExternalService.Iface;
import com.taobao.timetunnel.thrift.gen.Failure;
import com.taobao.timetunnel.tunnel.ByteBufferMessageTunnels;
import com.taobao.timetunnel.tunnel.TrimListener;
import com.taobao.util.JsonUtils;
import com.taobao.util.MemoryMonitor;

/**
 * {@link EfficientThriftBrokerTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
public final class EfficientThriftBroker extends ThriftBroker<ByteBuffer> {

  /**
   * 
   * @param center
   * @param host of server.
   * @param port of server provide external service.
   * @param syncPoint
   * @param monitor
   * @param home
   * @param maxMessageSize
   * @throws TTransportException
   */
  public EfficientThriftBroker(final Center center,
                               final String host,
                               final int port,
                               final String group,
                               final int syncPoint,
                               final MemoryMonitor monitor,
                               final File home,
                               final int maxMessageSize) throws TTransportException {
    super(center, group);
    this.maxMessageSize = maxMessageSize;
    info = new Info(host, port);
    processor = new ExternalService.Processor(new External());
    server = newThriftServer(info.host, info.external, workThread, maxReadBufferBytes, processor);
    final MessageFactory<ByteBuffer> messageFactory =
      new ByteBufferMessageCompactor(monitor, new File(home, "tunnels"), maxMessageSize);
    tunnels = new ByteBufferMessageTunnels(listener, syncPoint, messageFactory);
  }

  @Override
  public void doStop() {
    server.stop();
    tunnels.dispose();
  }

  @Override
  protected void doStart() {
    recoveryFromLast();
    Thread.currentThread().setName("broker-external");
    server.serve();
  }

  @Override
  protected String info() {
    return JsonUtils.json(info);
  }

  @Override
  protected void onClusterChanged(final List<String> newCluster) {
    /* Do not care about cluster change. */
  }

  @Override
  protected void onLossContactWithCluster() {
    reregister();
    LOGGER.info("Broker reregistered.");
  }

  private void recoveryFromLast() {
    // TODO Auto-generated method stub

  }

  private final TrimListener listener = new TrimListener() {

    @Override
    public void onTrim(final Category category, final Session session, final int size) {
      // ignore
    }
  };


  public static final Logger LOGGER = LoggerFactory.getLogger(EfficientThriftBroker.class);

  private final Info info;
  private final TServer server;
  private final int maxMessageSize;
  private final TProcessor processor;
  private final ByteBufferMessageTunnels tunnels;

  /**
   * {@link External}
   */
  private final class External implements Iface {

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
  private final class Info {
    public Info(final String host, final int port) {
      super();
      this.host = host;
      external = port;
    }

    final String host;
    final int external;
    @SuppressWarnings("unused")
    final int internal = -1; // use for json
  }

}
