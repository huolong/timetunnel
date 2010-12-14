package com.taobao.timetunnel.client;

import java.io.File;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.zookeeper.ZooKeeperConnector;
import com.taobao.timetunnel.zookeeper.ZooKeeperConnector.ZooKeeperListener;
import com.taobao.util.Bytes;

/**
 * {@link ZookeeperNodeCreater}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-5
 * 
 */
public final class ZookeeperNodeCreater implements ZooKeeperListener {

  public ZookeeperNodeCreater(final String connectString, final int sessionTimeout) {
    this.connectString = connectString;
    this.sessionTimeout = sessionTimeout;
  }

  public void createNodesBy(final File scriptFile) {
    try {
      doCreateNodeBy(new Scanner(scriptFile));
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void createNodesBy(final String script) {
    doCreateNodeBy(new Scanner(script));
  }

  @Override
  public void onConnected() {
    System.out.println(this.getClass().getSimpleName() + " connected to zookeeper.");
  }

  @Override
  public void onDisconnected() {
    System.out.println(this.getClass().getSimpleName() + " disconnected to zookeeper.");
  }

  @Override
  public void onNodeChildrenChanged(final String path) {}

  @Override
  public void onNodeCreated(final String path) {}

  @Override
  public void onNodeDataChanged(final String path) {}

  @Override
  public void onNodeDeleted(final String path) {}

  @Override
  public void onSessionExpired() {
    // TODO Auto-generated method stub

  }

  void eachLineTo(final CreateNodeClosure closure, final Scanner scanner) {
    while (scanner.hasNextLine()) {
      final String line = scanner.nextLine();
      if (line == null) continue;
      final Matcher matcher = pattern.matcher(line);
      if (!matcher.matches()) continue;
      final String path = matcher.group(1);
      final String data = matcher.group(4);
      closure.apply(path, safe(data));
      System.out.println(MessageFormat.format("path : {0} [{1}] is created.", path, data == null
        ? "" : data));
    }
    closure.dispose();
    scanner.close();
  }

  private void doCreateNodeBy(final Scanner scanner) {
    eachLineTo(new ZKCreateNodeClosure(), scanner);
  }

  private byte[] safe(final String data) {
    return data == null ? Bytes.NULL : data.getBytes();
  }

  private final Pattern pattern = Pattern.compile("((/\\w+)+)( : (.+))?");
  private final String connectString;

  private final int sessionTimeout;

  private interface CreateNodeClosure extends Disposable {
    void apply(String line, byte[] bs);
  }

  /**
   * {@link CreateNodeClosure}
   */
  private final class ZKCreateNodeClosure implements CreateNodeClosure {

    private ZKCreateNodeClosure() {
      connector = new ZooKeeperConnector(connectString, sessionTimeout, ZookeeperNodeCreater.this);
      connector.connect();
    }

    @Override
    public void apply(final String path, final byte[] data) {
      try {
        if (connector.exists(path, false) == null) {
          connector.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else connector.setData(path, data, -1);
      } catch (final KeeperException e) {
        e.printStackTrace();
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void dispose() {
      connector.disconnect();
    }

    private final ZooKeeperConnector connector;
  }

}
