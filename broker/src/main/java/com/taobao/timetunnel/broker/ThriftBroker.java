package com.taobao.timetunnel.broker;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import com.taobao.timetunnel.center.Center;
import com.taobao.timetunnel.center.Center.ClusterChangedWatcher;
import com.taobao.timetunnel.center.InvalidCategoryException;
import com.taobao.timetunnel.center.InvalidTokenException;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.thrift.gen.Failure;
import com.taobao.util.FixSizeBufferWriter;

/**
 * {@link ThriftBroker}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-17
 * 
 */
public abstract class ThriftBroker<T> implements Server {

  public ThriftBroker(final Center center, final String group) {
    this.center = center;
    this.group = group;
  }

  /**
   * {@link Integer#MAX_VALUE} is default.
   * 
   * @see EfficientThriftBroker#setMaxReadBufferBytes(long)
   */
  public final long getMaxReadBufferBytes() {
    return maxReadBufferBytes;
  }

  /**
   * 5 is default.
   * 
   * @see EfficientThriftBroker#setWorkThread(int)
   */
  public final int getWorkThread() {
    return workThread;
  }

  public final String id() {
    return id;
  }

  /**
   * Set maxReadBufferBytes before {@link EfficientThriftBroker#start()} can be
   * effective.
   * 
   * @param maxReadBufferBytes for flow control.
   */
  public final void setMaxReadBufferBytes(final long maxReadBufferBytes) {
    this.maxReadBufferBytes = maxReadBufferBytes;
  }

  /**
   * Set workThread before {@link EfficientThriftBroker#start()} can be
   * effective.
   * 
   * @param workThread for parallel hint.
   */
  public final void setWorkThread(final int workThread) {
    this.workThread = workThread;
  }

  @Override
  public final void start() {
    id = center.register(info(), group, watcher());
    doStart();
    center.unregister();
  }

  @Override
  public final void stop() {
    doStop();
  }

  protected final Category category(final String category) {
    return center.category(category);
  }

  protected abstract void doStart();

  protected abstract void doStop();

  protected final Failure failure(final Throwable t) {
    return new Failure(code(t), reason(t), detail(t));
  }

  protected abstract String info();

  protected abstract void onClusterChanged(List<String> newCluster);

  protected abstract void onLossContactWithCluster();

  protected final InetSocketAddress reliableServiceAddress(final String name) {
    return center.reliableServiceAddress(name);
  }

  protected synchronized final void reregister() {
    center.unregister();
    id = center.register(info(), group, watcher());
  }

  protected final Session session(final ByteBuffer token) {
    return center.session(token);
  }

  private short code(final Throwable t) {
    if (t instanceof InvalidTokenException) return 1;
    if (t instanceof InvalidCategoryException) return 2;
    if (t instanceof TooBigMessageException) return 3;
    if (t instanceof ReliableServiceClientException) return 4;
    return -1; // unknown
  }

  private String detail(final Throwable t) {
    final Writer writer = new FixSizeBufferWriter(1024);
    t.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  private String reason(final Throwable t) {
    return t.getClass().getSimpleName();
  }

  private ClusterChangedWatcher watcher() {
    return clusterChangedWatcher;
  }

  private final ClusterChangedWatcher clusterChangedWatcher = new ClusterChangedWatcher() {
    @Override
    public void onClusterChanged(final List<String> newCluster) {
      ThriftBroker.this.onClusterChanged(newCluster);
    }

    @Override
    public void onLossContactWithCluster() {
      ThriftBroker.this.onLossContactWithCluster();
    }
  };

  private final String group;
  private final Center center;

  private String id = "unknown";
  protected int workThread = 5;

  protected long maxReadBufferBytes = Integer.MAX_VALUE;

}
