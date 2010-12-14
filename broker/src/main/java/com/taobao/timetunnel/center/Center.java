package com.taobao.timetunnel.center;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import com.taobao.timetunnel.broker.ThriftBroker;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;

/**
 * {@link Center} is an interface used by {@link ThriftBroker}.
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
public interface Center {

  /**
   * @param name
   * @return {@link Category}.
   */
  Category category(String name);

  /**
   * Register a broker in cluster.
   * 
   * @param info of broker
   * @param group
   * @param watcher
   * @return
   */
  String register(String info, String group, ClusterChangedWatcher watcher);

  /**
   * @param name
   * @return {@link InetSocketAddress} of reliable service.
   */
  InetSocketAddress reliableServiceAddress(String name);

  /**
   * @param token
   * @return {@link Session} of client.
   */
  Session session(ByteBuffer token);

  /**
   * Unregister broker.
   */
  void unregister();

  public interface ClusterChangedWatcher {
    void onClusterChanged(List<String> newCluster);

    void onLossContactWithCluster();
  }
}
