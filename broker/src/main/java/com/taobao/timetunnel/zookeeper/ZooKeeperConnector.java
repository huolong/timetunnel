package com.taobao.timetunnel.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * ZooKeeperConnector
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-10
 * 
 */
public final class ZooKeeperConnector implements Watcher {

  public ZooKeeperConnector(final String connectString,
                            final int sessionTimeout,
                            final ZooKeeperListener listener) {
    this.connectString = connectString;
    this.sessionTimeout = sessionTimeout;
    this.listener = listener;
    connectFuture = new FutureTask<Void>(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        listener.onConnected();
        return null;
      }
    });
  }

  public void connect() {
    try {
      keeper = new ZooKeeper(connectString, sessionTimeout, this);
      connectFuture.get();
    } catch (final IOException e) {
      throw new RuntimeException("Connecting to ZooKeeper failded.", e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (final ExecutionException e) {
      throw new RuntimeException("Unexpected error in onConnected callback.", e.getCause());
    }
  }

  public String create(final String path,
                       final byte[] data,
                       final List<ACL> acl,
                       final CreateMode createMode) throws KeeperException, InterruptedException {
    return keeper.create(path, data, acl, createMode);
  }

  public void delete(final String path, final int version) throws InterruptedException,
                                                          KeeperException {
    keeper.delete(path, version);
  }

  public void disconnect() {
    if (keeper != null) {
      try {
        keeper.close();
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        keeper = null;
      }
    }
  }

  public Stat exists(final String path, final boolean watch) throws KeeperException,
                                                            InterruptedException {
    return keeper.exists(path, watch);
  }

  public List<String> getChildren(final String path, final boolean watch) throws KeeperException,
                                                                         InterruptedException {
    return keeper.getChildren(path, watch);
  }

  public byte[] getData(final String path, final boolean watch, final Stat stat) throws KeeperException,
                                                                                InterruptedException {
    return keeper.getData(path, watch, stat);
  }

  public boolean isConnected() {
    return keeper != null && keeper.getState().isAlive();
  }

  @Override
  public void process(final WatchedEvent event) {
    final KeeperState state = event.getState();
    switch (state) {
      case SyncConnected:
        processWatchedEvent(event.getType(), event.getPath());
        break;
      case Disconnected:
        listener.onDisconnected();
        break;
      case Expired:
        listener.onSessionExpired();
        break;
      default:
        // TODO log unexpected zookeeper state, may be a version compatibility
        // problem.
        break;
    }

  }

  public Stat setData(final String path, final byte[] data, final int version) throws KeeperException,
                                                                              InterruptedException {
    return keeper.setData(path, data, version);
  }

  private void processWatchedEvent(final EventType type, final String path) {
    if (!isConnected()) return;
    switch (type) {
      case NodeChildrenChanged:
        listener.onNodeChildrenChanged(path);
        break;
      case NodeCreated:
        listener.onNodeCreated(path);
        break;
      case NodeDataChanged:
        listener.onNodeDataChanged(path);
        break;
      case NodeDeleted:
        listener.onNodeDeleted(path);
        break;
      case None:
        connectFuture.run();
        break;
      default:
        // TODO log unexpected event type, may be a version compatibility
        // problem.
        break;
    }

  }

  private final FutureTask<Void> connectFuture;

  private final String connectString;

  private final int sessionTimeout;

  private final ZooKeeperListener listener;

  private volatile ZooKeeper keeper;

  /**
   * NodeEventListener
   */
  public interface ZooKeeperListener {

    void onConnected();

    void onDisconnected();

    void onNodeChildrenChanged(String path);

    void onNodeCreated(String path);

    void onNodeDataChanged(String path);

    void onNodeDeleted(String path);

    void onSessionExpired();

  }

}
