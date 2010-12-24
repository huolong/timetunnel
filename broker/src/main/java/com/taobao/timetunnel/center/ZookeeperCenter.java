package com.taobao.timetunnel.center;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.zookeeper.CreateMode.EPHEMERAL_SEQUENTIAL;
import static org.apache.zookeeper.CreateMode.PERSISTENT;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;
import static org.apache.zookeeper.ZooDefs.Ids.READ_ACL_UNSAFE;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.taobao.timetunnel.InvalidCategoryException;
import com.taobao.timetunnel.InvalidTokenException;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.session.Type;
import com.taobao.timetunnel.zookeeper.ZooKeeperConnector;
import com.taobao.timetunnel.zookeeper.ZooKeeperConnector.ZooKeeperListener;
import com.taobao.util.Bytes;
import com.taobao.util.Events;
import com.taobao.util.Repository;
import com.taobao.util.SystemTime;

/**
 * {@link ZookeeperCenter}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public class ZookeeperCenter implements Center, ZooKeeperListener {

  public ZookeeperCenter(final String connectString,
                         final int sessionTimeout,
                         final int rebalancePeriod) {
    this.rebalancePeriod = SECONDS.toMillis(rebalancePeriod);
    connector = new ZooKeeperConnector(connectString, sessionTimeout, this);
    Events.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        sessions.checkTimeoutAndExpired();
      }
    }, 1, SECONDS);
  }

  @Override
  public Category category(final String name) {
    try {
      return categories.getOrCreateIfNotExist(name);
    } catch (final Exception e) {
      throw new InvalidCategoryException(e);
    }
  }

  @Override
  public void onConnected() {
    LOGGER.info("Broker connected to zookeeper server");
  }

  @Override
  public void onDisconnected() {
    LOGGER.info("Broker disconnected to zookeeper server");
    // watcher.onLossContactWithCluster();
  }

  @Override
  public void onNodeChildrenChanged(final String path) {
    LOGGER.info("Node {} changed.", path);
    if (path.equals(currentGroup)) {
      fireOnClusterChanged();
    }

    final Matcher matcher = CATEGORY_PATTERN.matcher(path);
    if (matcher.matches()) {
      category(matcher.group(1));
    }
  }

  @Override
  public void onNodeCreated(final String path) {/* ignore */}

  @Override
  public void onNodeDataChanged(final String path) {/* ignore */}

  @Override
  public void onNodeDeleted(final String path) {
    if (path.startsWith("/clients")) {
      sessions.invalid(path);
    }
  }

  @Override
  public void onSessionExpired() {
    LOGGER.error("Zookeeper session expired, loss contact with cluster.");
    watcher.onLossContactWithCluster();
  }

  @Override
  public String register(final String info, final String group, final ClusterChangedWatcher watcher) {
    currentGroup = BROKERS + "/" + group;

    this.watcher = watcher;
    connector.connect();
    LOGGER.info("Register broker {}", info);
    try {
      createIfNotExist(BROKERS);
      createIfNotExist(currentGroup);
      connector.getChildren(currentGroup, true);

      initCategories();

      final String path =
        connector.create(currentGroup + "/b",
                         Bytes.toBytes(info),
                         READ_ACL_UNSAFE,
                         EPHEMERAL_SEQUENTIAL);

      return path.substring(currentGroup.length() + 1);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public InetSocketAddress reliableServiceAddress(final String name) {
    try {
      final byte[] data = connector.getData(currentGroup + "/" + name, false, null);
      final JsonObject object = parser.parse(Bytes.toString(data)).getAsJsonObject();
      return new InetSocketAddress(object.get("host").getAsString(), object.get("internal")
                                                                           .getAsInt());
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Session session(final ByteBuffer token) {
    try {
      final InnerSession session = sessions.getOrCreateIfNotExist(token);
      session.lastTickTime = SystemTime.current();
      return session;
    } catch (final Exception e) {
      throw new InvalidTokenException(e);
    }
  }

  @Override
  public void unregister() {
    LOGGER.info("Unregister broker.");
    connector.disconnect();
  }

  protected void createIfNotExist(final String node) throws KeeperException, InterruptedException {
    if (connector.exists(node, true) == null)
      connector.create(node, Bytes.NULL, OPEN_ACL_UNSAFE, PERSISTENT);
  }

  protected void initCategories() throws KeeperException, InterruptedException {
    final List<String> children = connector.getChildren(CATEGORIES, true);
    for (final String child : children) {
      category(child);
    }
  }

  private synchronized void fireOnClusterChanged() {
    try {
      final List<String> children = connector.getChildren(currentGroup, true);
      watcher.onClusterChanged(children);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.error("Interrupt getting children of " + BROKERS, e);
    } catch (final Exception e) {
      LOGGER.error("Unexpected error on cluster changed", e);
    }
  }

  private static final String CATEGORIES = "/categories";
  private static final String BROKERS = "/brokers";
  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCenter.class);

  private static final Pattern CATEGORY_PATTERN = Pattern.compile(CATEGORIES
      + "/(\\w+)/subscribers");
  private final long rebalancePeriod;
  private final ZooKeeperConnector connector;
  private final Categories categories = new Categories();
  private final Sessions sessions = new Sessions();

  private final JsonParser parser = new JsonParser();
  private ClusterChangedWatcher watcher;

  private String currentGroup;

  /**
   * {@link Categories}
   */
  private final class Categories extends Repository<String, InnerCategory> {

    public Categories() {
      super(new Factory<String, InnerCategory>() {

        @Override
        public InnerCategory newInstance(final String key) {
          return new InnerCategory(key);
        }
      });
    }

  }

  /**
   * {@link InnerCategory}
   */
  private final class InnerCategory implements Category {

    public InnerCategory(final String key) {
      this.key = key;
      try {
        final String path = MessageFormat.format((CATEGORIES + "/{0}/subscribers"), key);
        final List<String> children = connector.getChildren(path, true);
        readers = new HashSet<String>(children);
        LOGGER.info("{} created.", this);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isInvaildSubscriber(final String key) {
      return !readers.contains(key);
    }

    @Override
    public boolean isMessageExpiredAfter(final long created) {
      // TODO category node in zookeeper has timeToLive value.
      return false;
    }

    @Override
    public boolean isMessageUselessReadBy(final Set<String> readers) {
      return readers.containsAll(this.readers);
    }

    @Override
    public String name() {
      return key;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("InnerCategory [key=")
             .append(key)
             .append(", readers=")
             .append(readers)
             .append("]");
      return builder.toString();
    }

    private final String key;
    private volatile Set<String> readers = new HashSet<String>();
  }

  private final class InnerSession implements Session {

    public InnerSession(final ByteBuffer key) {
      id = Bytes.toString(key);
      listeners = new CopyOnWriteArrayList<Session.InvalidListener>();
      try {
        final byte[] data = connector.getData(id, true, null);
        final String json = Bytes.toString(data);
        attributes = parser.parse(json).getAsJsonObject();
        lastTickTime = created = SystemTime.current();
        LOGGER.debug("{} created.", this);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void add(final InvalidListener listener) {
      listeners.add(listener);
    }

    @Override
    public boolean booleanValueOf(final Attribute attribute) {
      if (!attributes.has(attribute.name()))
        throw new NullPointerException(attribute.name() + " does not exist.");
      return attributes.get(attribute.name()).getAsBoolean();
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public int intValueOf(final Attribute attribute) {
      if (!attributes.has(attribute.name()))
        throw new NullPointerException(attribute.name() + " does not exist.");
      return attributes.get(attribute.name()).getAsInt();
    }

    public void invalid(final boolean delete) {
      try {
        LOGGER.info("{} invalid {}.", this, (delete ? "positive" : "negative"));
        invalid = true;
        for (final InvalidListener listener : listeners)
          listener.onInvalid();
        if (delete && connector.isConnected()) connector.delete(id, -1);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isInvalid() {
      return invalid;
    }

    @Override
    public long longValueOf(final Attribute attribute) {
      if (!attributes.has(attribute.name()))
        throw new NullPointerException(attribute.name() + " does not exist.");
      return attributes.get(attribute.name()).getAsLong();
    }

    @Override
    public void remove(final InvalidListener listener) {
      listeners.remove(listener);
    }

    @Override
    public String stringValueOf(final Attribute attribute) {
      if (!attributes.has(attribute.name()))
        throw new NullPointerException(attribute.name() + " does not exist.");
      return attributes.get(attribute.name()).getAsString();
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("InnerSession [id=")
             .append(id)
             .append(", created=")
             .append(created)
             .append(", lastTickTime=")
             .append(lastTickTime)
             .append(", invalid=")
             .append(invalid)
             .append(", attributes=")
             .append(attributes)
             .append("]");
      return builder.toString();
    }

    @Override
    public Type type() {
      if (!attributes.has("type")) throw new NullPointerException("type does not exist.");
      return Type.valueOf(attributes.get("type").getAsString());
    }

    private volatile boolean invalid;
    private volatile long lastTickTime;

    private final long created;
    private final String id;
    private final JsonObject attributes;
    private final Collection<InvalidListener> listeners;

  }

  /**
   * {@link Sessions}
   */
  private final class Sessions extends Repository<ByteBuffer, InnerSession> {

    public Sessions() {
      super(new Factory<ByteBuffer, InnerSession>() {

        @Override
        public InnerSession newInstance(final ByteBuffer key) {
          return new InnerSession(key);
        }
      });
    }

    public void checkTimeoutAndExpired() {
      LOGGER.debug("Start check session timeout or expired.");
      for (final ByteBuffer token : map.keySet()) {
        try {
          final InnerSession session = map.get(token).get();
          if (session.isInvalid()) {
            remove(session.id());
            continue;
          }

          final long timeout = SECONDS.toMillis(session.intValueOf(Attribute.timeout));
          final long current = SystemTime.current();
          if (isTimeout(session, timeout, current) || shouldRebalance(session, current))
            session.invalid(true);

        } catch (final Exception e) {
          LOGGER.error("Missing token " + Bytes.toString(token), e);
          map.remove(token);
        }
      }
    }

    public void invalid(final String path) {
      final FutureTask<InnerSession> task = map.get(path);
      if (task == null) return;
      try {
        final InnerSession session = task.get();
        remove(session.id());
        session.invalid(false);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    protected boolean isTimeout(final InnerSession session, final long timeout, final long current) {
      final boolean b = current > session.lastTickTime + timeout;
      if (b) LOGGER.info("Invalid {} because of timeout.", session);
      return b;
    }

    protected boolean shouldRebalance(final InnerSession session, final long current) {
      final boolean b = current > session.created + rebalancePeriod;
      if (b) LOGGER.info("Invalid {} because of rebalance period.", session);
      return b;
    }

    private void remove(final String id) {
      map.remove(Bytes.toBuffer(id));
    }
  }

}
