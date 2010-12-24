package com.taobao.timetunnel.bootstrap;

import static java.net.InetAddress.getLocalHost;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.taobao.timetunnel.broker.EfficientThriftBroker;
import com.taobao.timetunnel.broker.ReliableThriftBroker;
import com.taobao.timetunnel.broker.ThriftBroker;
import com.taobao.timetunnel.center.Center;
import com.taobao.timetunnel.center.ZookeeperCenter;
import com.taobao.util.MemoryMonitor;
import com.taobao.util.PropertiesHelper;
import com.taobao.util.SizeConverter;
import com.taobao.util.PropertiesHelper.Converter;

/**
 * {@link BrokerBootstrap}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public class BrokerBootstrap {

  /**
   * @param args
   */
  public static void main(final String[] args) {
    String conf = "conf.properties";
    if (args.length == 1) conf = args[0];

    try {
      final ThriftBroker<ByteBuffer> thriftBroker =
        createThriftBrokerWith(new PropertiesHelper(new File(conf)));
      addShutdownHook(thriftBroker);
      Thread.currentThread().setName("reliable-broker");
      thriftBroker.start();
    } catch (final Exception e) {
      printUsageAndExit(new File(conf), e);
    }
  }

  private static void addShutdownHook(final ThriftBroker<ByteBuffer> thriftBroker) {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

      @Override
      public void run() {
        thriftBroker.stop();
      }
    }, "shutdown-broker-hook"));
  }

  private static Center createCenterWith(final PropertiesHelper helper) {
    return new ZookeeperCenter(helper.getString("zookeeper.connectString"),
                               helper.getInt("zookeeper.sessionTimeout"),
                               helper.getInt("cluster.rebalancePeriod", 60));
  }

  private static MemoryMonitor createMonitorWith(final PropertiesHelper helper) {
    final long max = MemoryMonitor.max() / 100;
    final long shortage = max * helper.getInt("memory.shortageRatio", 45);
    final long abundant = max * helper.getInt("memory.abundantRatio", 75);
    return new MemoryMonitor(shortage, abundant);
  }

  private static Converter<Integer> sizeConverter = new SizeConverter();

  private static ThriftBroker<ByteBuffer> createThriftBrokerWith(final PropertiesHelper helper) throws UnknownHostException {
    final File data = new File(helper.getString("broker.home", "."), "data");
    data.mkdirs();

    final Center center = createCenterWith(helper);
    final MemoryMonitor monitor = createMonitorWith(helper);

    ThriftBroker<ByteBuffer> thriftBroker = null;
    final String host = helper.getString("broker.host", getLocalHost().getHostName());
    final int port = helper.getInt("external.port");
    final String group = helper.getString("broker.group");
    final int syncPoint = helper.getInt("group.syncPoint", 200);
    final int maxMessageSize = helper.get(sizeConverter, "chunk.buffer", "4K");

    int chunkCapacity = helper.get(sizeConverter, "chunk.capacity", "64M");
    int chunkBuffer = helper.get(sizeConverter, "chunk.buffer", "32K");
    if (helper.contains("internal.port")) {
      thriftBroker =
        new ReliableThriftBroker(center,
                                 host,
                                 port,
                                 helper.getInt("internal.port"),
                                 group,
                                 syncPoint,
                                 maxMessageSize,
                                 chunkCapacity,
                                 chunkBuffer,
                                 monitor,
                                 data);
      System.out.println("Starting a reliable broker.");
    } else {
      thriftBroker =
        new EfficientThriftBroker(center,
                                  host,
                                  port,
                                  group,
                                  syncPoint,
                                  monitor,
                                  data,
                                  maxMessageSize,
                                  chunkCapacity,
                                  chunkBuffer);
      System.out.println("Starting a efficient broker.");
    }
    thriftBroker.setMaxReadBufferBytes(helper.getInt("broker.maxReadBufferBytes", Integer.MAX_VALUE));
    thriftBroker.setWorkThread(helper.getInt("broker.workThread", 5));
    return thriftBroker;
  }

  private static void printUsageAndExit(final File file, final Exception e) {
    e.printStackTrace();
    System.err.println("Invalid conf file path, please check your conf file " + file);
    System.err.println("Usage : CMD [conf file path]");
    System.exit(-1);
  }

}
