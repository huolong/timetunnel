package com.taobao.timetunnel.broker;

import static com.taobao.timetunnel.client.TestClient.noneTestRuntimeReport;
import static com.taobao.timetunnel.client.TestClient.parallelPubs;
import static com.taobao.timetunnel.client.TestClient.parallelSubs;

import java.io.File;
import java.nio.ByteBuffer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.timetunnel.center.ZookeeperCenter;
import com.taobao.timetunnel.client.BufferFinance;
import com.taobao.timetunnel.client.ClientFactory;
import com.taobao.timetunnel.client.TestClient;
import com.taobao.timetunnel.client.ZookeeperNodeCreater;
import com.taobao.timetunnel.zookeeper.ZooKeeperConnector.ZooKeeperListener;
import com.taobao.timetunnel.zookeeper.ZooKeeperServerForTest;
import com.taobao.util.DirectoryCleaner;
import com.taobao.util.MemoryMonitor;

/**
 * {@link ReliableThriftBrokerTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public class ReliableThriftBrokerTest implements ZooKeeperListener {

  @Override
  public void onConnected() {}

  @Override
  public void onDisconnected() {}

  @Override
  public void onNodeChildrenChanged(final String path) {}

  @Override
  public void onNodeCreated(final String path) {}

  @Override
  public void onNodeDataChanged(final String path) {}

  @Override
  public void onNodeDeleted(final String path) {}

  @Override
  public void onSessionExpired() {}

  @Test
  public void shouldGetRightMessage() throws Exception {
    final int times = 10;
    final String host = "localhost";
    final String category = "chat";
    final int external = 9999;

    final ZookeeperCenter center = new ZookeeperCenter(host + ":8888", 2000, 60);
    final ThriftBroker<ByteBuffer> thriftBroker =
      new ReliableThriftBroker(center,
                               host,
                               9999,
                               9998,
                               "group",
                               100,
                               4096,
                               monitor,
                               freezer("freezers"));
    start(thriftBroker);

    TestClient.retry(1);
    Thread.sleep(1000L); // wait clients initializaion in center.

    final ClientFactory clientFactory = TestClient.clientFactory(host, external);
    final BufferFinance finance = TestClient.finance(1, times);
    TestClient.parallelPubsAndSubs(clientFactory,
                                   "/clients/pub",
                                   "/clients/sub",
                                   category,
                                   3,
                                   2,
                                   noneTestRuntimeReport(),
                                   noneTestRuntimeReport(),
                                   finance);
    thriftBroker.stop();

  }

  @Test
  public void shouldGetRightMessageEvenIfBrokerCrashInCluster() throws Exception {
    final ZookeeperCenter center0 = new ZookeeperCenter("localhost:8888", 2000, 60);
    final ZookeeperCenter center1 = new ZookeeperCenter("localhost:8888", 2000, 60);

    final ThriftBroker<ByteBuffer> thriftBroker0 =
      new ReliableThriftBroker(center0,
                               "localhost",
                               9900,
                               9910,
                               "group",
                               100,
                               4096,
                               monitor,
                               freezer("freezers0"));

    final ThriftBroker<ByteBuffer> thriftBroker1 =
      new ReliableThriftBroker(center1,
                               "localhost",
                               9901,
                               9911,
                               "group",
                               100,
                               4096,
                               monitor,
                               freezer("freezers1"));
    start(thriftBroker0);

    Thread.sleep(1000L); // wait for broker0 register

    start(thriftBroker1);

    Thread.sleep(1000L); // wait clients initializaion in center.

    final int times = 10;
    final String host = "localhost";
    final String category = "chat";
    final ClientFactory factory0 = TestClient.clientFactory(host, 9900);
    final ClientFactory factory1 = TestClient.clientFactory(host, 9901);
    final BufferFinance finance = TestClient.finance(1, times);

    TestClient.retry(1);

    parallelPubs(factory0, "/clients/pub", category, 3, noneTestRuntimeReport(), finance);

    Thread.sleep(1000L); // wait clients initializaion in center.

    thriftBroker0.stop();

    parallelSubs(factory1, "/clients/sub", category, 2, noneTestRuntimeReport(), finance);

    thriftBroker1.stop();

  }

  protected File freezer(final String dir) {
    final File freezerRoot = new File(root, dir);
    DirectoryCleaner.clean(freezerRoot);
    freezerRoot.mkdirs();
    return freezerRoot;
  }

  private void start(final ThriftBroker<ByteBuffer> thriftBroker) {
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          thriftBroker.start();
        } catch (final Exception e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    }, "reliable-broker").start();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DirectoryCleaner.clean(root);
    startZookeeperServer();
    new ZookeeperNodeCreater(connectString, sessionTimeout).createNodesBy(new File("src/test/resources/zknodes.script"));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    try {
      zookeeper.shutdown();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  private static void startZookeeperServer() {
    zookeeper = new ZooKeeperServerForTest(8888, "./target/rtbt", 1000);
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          zookeeper.startup();
        } catch (final Exception e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    }, "zookeeper-server").start();
  }

  private final MemoryMonitor monitor = new MemoryMonitor(MemoryMonitor.max() - 10240,
                                                          MemoryMonitor.max() - 1);

  private final static File root = new File("./target/rtbt");
  private final static String connectString = "localhost:8888";
  private final static int sessionTimeout = 2000;

  private static ZooKeeperServerForTest zookeeper;
}
