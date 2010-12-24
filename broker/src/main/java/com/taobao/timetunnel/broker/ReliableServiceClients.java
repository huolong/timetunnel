package com.taobao.timetunnel.broker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.ReliableServiceClientException;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.reliable.ReliableService;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.thrift.gen.Failure;
import com.taobao.timetunnel.thrift.gen.InternalService.Client;
import com.taobao.timetunnel.thrift.util.ClientUtils;
import com.taobao.util.Bytes;

/**
 * {@link ReliableServiceClients}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-17
 * 
 */
public final class ReliableServiceClients {

  private ReliableServiceClients() {}

  public static final void hint(final long timeout) {
    ReliableServiceClients.timeout = timeout;
  }

  public static final ReliableServiceClient newClient(final InetSocketAddress inetSocketAddress) throws TTransportException {
    return new RsClient(inetSocketAddress);
  }

  public static final ReliableServiceClient NONE = new None();
  private static final long DEFAUTL_TIMEOUT = 4000L;
  /** timeout for poll client. */
  private static long timeout = DEFAUTL_TIMEOUT;

  /**
   * {@link ReliableServiceClient}
   */
  public interface ReliableServiceClient extends ReliableService<ByteBuffer>, Disposable {}

  /**
   * {@link None} pattern.
   */
  private static final class None implements ReliableServiceClient {
    @Override
    public void copy(final Category category, final Session session, final ByteBuffer message) {}

    @Override
    public void dispose() {}

    @Override
    public void dump(final Category category, final Session session, final ByteBuffer message) {}

    @Override
    public void trim(final Category category, final Session session, final int size) {}
  }

  /**
   * {@link RsClient}
   */
  private static final class RsClient implements ReliableServiceClient {

    public RsClient(final InetSocketAddress inetSocketAddress) throws TTransportException {
      final Client.Factory factory = new Client.Factory();
      try {
        for (int i = 0; i < size; i++) {
          queue.add(ClientUtils.newClient(factory, inetSocketAddress));
        }
      } catch (final Exception e) {
        dispose();
        throw new TTransportException(e);
      }
    }

    @Override
    public void copy(final Category category, final Session session, final ByteBuffer message) {
      block.call(new Closure() {

        @Override
        public void call(final Client client) throws Failure, TException {
          client.copy(category.name(), Bytes.toBuffer(session.id()), message);
        }
      });

    }

    @Override
    public void dispose() {
      try {
        for (int i = 0; i < size; i++) {
          Client client = null;
          do {
            client = queue.poll(100L, TimeUnit.MILLISECONDS);
          } while (client == null);
          ClientUtils.close(client);
        }
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }

    @Override
    public void dump(final Category category, final Session session, final ByteBuffer message) {
      block.call(new Closure() {

        @Override
        public void call(final Client client) throws Failure, TException {
          client.dump(category.name(), Bytes.toBuffer(session.id()), message);
        }
      });
    }

    @Override
    public void trim(final Category category, final Session session, final int size) {
      block.call(new Closure() {

        @Override
        public void call(final Client client) throws Failure, TException {
          client.trim(category.name(), Bytes.toBuffer(session.id()), size);
        }
      });

    }

    private final BlockingQueue<Client> queue = new LinkedBlockingQueue<Client>();
    private final int size = Runtime.getRuntime().availableProcessors() * 2;
    private final SafeBlock block = new SafeBlock();

    /**
     * {@link Closure}
     */
    private interface Closure {
      void call(Client client) throws Failure, TException;
    }

    /**
     * {@link SafeBlock}
     */
    private final class SafeBlock {
      public void call(final Closure closure) {
        Client client = null;
        try {
          client = queue.poll(timeout, TimeUnit.MILLISECONDS);
          if (client == null)
            throw new IllegalStateException("Can't acquire InternalService Client, remote server provided InternalServcie maybe crashed.");
          closure.call(client);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Interrupt poll InternalService Client.", e);
        } catch (final TException e) {
          throw new ReliableServiceClientException(e);
        } catch (final Failure e) {
          throw new ReliableServiceClientException(e);
        } finally {
          if (client != null) queue.add(client);
        }
      }
    }

  }

}
