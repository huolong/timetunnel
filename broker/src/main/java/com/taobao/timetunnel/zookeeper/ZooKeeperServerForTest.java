package com.taobao.timetunnel.zookeeper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

import com.taobao.util.RecurseTree;
import com.taobao.util.RecurseTree.Callback;
import com.taobao.util.RecurseTree.IteratorFactory;

/**
 * ZooKeeperServerForTest
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-10
 * 
 */
public final class ZooKeeperServerForTest extends ZooKeeperServerMain {

  public ZooKeeperServerForTest(final int port,
                                final String dataDir,
                                final int tickTime) {
    this.port = port;
    this.dataDir = dataDir;
    this.tickTime = tickTime;
  }

  /**
   * Shutdown ZooKeeper server.
   */
  @Override
  public void shutdown() {
    super.shutdown();
    recurseDelete(dataDir);
  }

  /**
   * Startup ZooKeeper server.
   * 
   * @throws ConfigException
   * @throws IOException
   */
  public void startup() throws ConfigException, IOException {
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          initializeAndRun(new String[] { "" + port, dataDir, "" + tickTime });
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, "zookeeper-server").start();

  }

  @Override
  protected void initializeAndRun(final String[] args) throws ConfigException,
                                                      IOException {
    super.initializeAndRun(args);
  }

  private void recurseDelete(final String dataDir) {
    final File root = new File(dataDir);

    final Iterator<File> empty = RecurseTree.empty();

    final IteratorFactory<File> factory = new IteratorFactory<File>() {

      @Override
      public Iterator<File> iterator(final File obj) {
        return obj.isDirectory() ? Arrays.asList(obj.listFiles()).iterator()
          : empty;
      }
    };
    final Callback<File> callback = new Callback<File>() {

      @Override
      public void onCallback(final File obj) {
        obj.deleteOnExit();
      }
    };
    RecurseTree.run(root, factory, callback);
  }

  private final int port;

  private final String dataDir;

  private final int tickTime;

}
