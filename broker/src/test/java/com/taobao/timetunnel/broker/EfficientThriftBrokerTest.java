package com.taobao.timetunnel.broker;

import static com.taobao.timetunnel.client.TestClient.noneTestRuntimeReport;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.timetunnel.InjectMocksSupport;
import com.taobao.timetunnel.center.ZookeeperCenter;
import com.taobao.timetunnel.client.BufferFinance;
import com.taobao.timetunnel.client.ClientFactory;
import com.taobao.timetunnel.client.TestClient;
import com.taobao.timetunnel.client.ZookeeperNodeCreater;
import com.taobao.timetunnel.zookeeper.ZooKeeperServerForTest;
import com.taobao.util.DirectoryCleaner;
import com.taobao.util.MemoryMonitor;

/**
 * BrokerTest
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-14
 * 
 */
public class EfficientThriftBrokerTest extends InjectMocksSupport {
  @Test
  public void point2point() throws Exception {
    // TODO point2point ()
    TestClient.retry(1);
    final ClientFactory factory = TestClient.clientFactory(host, external);
    final BufferFinance finance = TestClient.finance(1, TIMES);

    TestClient.parallelPubsAndSubs(factory,
                                   "/clients/pub",
                                   "/clients/sub",
                                   category,
                                   1,
                                   1,
                                   noneTestRuntimeReport(),
                                   noneTestRuntimeReport(),
                                   finance);

  }

  @Before
  public void setUp() throws Exception {
    final ZookeeperCenter center = new ZookeeperCenter(host + ":8881", 2000, 60);

    server =
      new EfficientThriftBroker(center, host, external, "group", 50, monitor, root, maxMessageSize);
    new Thread(new Runnable() {

      @Override
      public void run() {
        server.start();
      }
    }, "efficient-thrift-broker").start();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DirectoryCleaner.clean(root);
    root.mkdirs();

    zookeeper = new ZooKeeperServerForTest(8881, "target/etbt", 1000);
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
    new ZookeeperNodeCreater(connectString, sessionTimeout).createNodesBy(new File("src/test/resources/zknodes.script"));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    try {
      zookeeper.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static final File root = new File("target/etbt");

  private static ZooKeeperServerForTest zookeeper;

  private static String connectString = "localhost:8881";

  private static int sessionTimeout = 2000;

  private final MemoryMonitor monitor = new MemoryMonitor(MemoryMonitor.max() - 10240,
                                                          MemoryMonitor.max() - 1);
  private final int maxMessageSize = 4096;

  private Server server;

  private final String host = "localhost";
  private final String category = "chat";
  private final int external = 9999;

  private static final int TIMES = 1000;
}
