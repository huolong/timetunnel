package com.taobao.timetunnel.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.taobao.util.Bytes;

/**
 * ZooKeeperWatcherTest
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-10
 * 
 */
@Ignore
public class ZooKeeperWatcherTest {

  @Test
  public void nothing() throws Exception {
    Thread.sleep(1000L);
    
    String connectString = "localhost:30000";
    int sessionTimeout = 500;
    Watcher watcher0 = new Watcher() {
      
      @Override
      public void process(WatchedEvent event) {
        System.out.println("watcher0 : " + event);
      }
    };
    ZooKeeper keeper0 = new ZooKeeper(connectString, sessionTimeout, watcher0);
    System.out.println(keeper0.create("/keeper", Bytes.NULL, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL));
    Thread.sleep(1100L);
    keeper0.close();

    Watcher watcher1 = new Watcher() {
      
      @Override
      public void process(WatchedEvent event) {
        System.out.println("watcher1 : " + event);
      }
    };
    ZooKeeper keeper1 = new ZooKeeper(connectString, sessionTimeout, watcher1 );
    System.out.println(keeper1.exists("/keeper", false));
    Thread.sleep(1000L);
    keeper1.close();
  }

  @Before
  public void setUp() throws Exception {
    server = new ZooKeeperServerForTest(30000, "./target/zkwt", 1000);
    server.startup();
  }

  @After
  public void tearDown() throws Exception {
    server.shutdown();
  }

  private ZooKeeperServerForTest server;

}
