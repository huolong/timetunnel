package com.taobao.timetunnel.client;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;

import com.taobao.timetunnel.client.BufferFinance.Accountant;
import com.taobao.timetunnel.client.BufferFinance.Cashier;
import com.taobao.timetunnel.zookeeper.ZooKeeperServerForTest;
import com.taobao.util.Race;

/**
 * {@link TestClient}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-2
 * 
 */
public final class TestClient {

  private TestClient() {}

  public static ClientFactory clientFactory(final String host, final int port) {
    return new ClientFactory(host, port);
  }

  public static BufferFinance finance(final int size, final int capacity) {
    return new BufferFinance(size, capacity);
  }

  public static void main(final String[] args) throws Exception {

    try {
      Command.valueOf(args[0]).startWith(args);
    } catch (final Exception e) {
      e.printStackTrace();
      printUsage(args);
    }
  }

  public static TestRuntimeReport newTestRuntimeReport(final long standard, final int printPeriod) {
    return new RealTimeTestRunTimeReport(standard, printPeriod);
  }

  public static TestRuntimeReport noneTestRuntimeReport() {
    return noneTestRuntimeReport;
  }

  public static void parallelPubs(final ClientFactory factory,
                                  final String token,
                                  final String category,
                                  final int num,
                                  final TestRuntimeReport report,
                                  final BufferFinance finance) throws Exception {
    final Callable<Void>[] pubs = new Pub[num];
    for (int i = 0; i < pubs.length; i++)
      pubs[i] = pub(factory, token + i, report, category, finance.cashier());
    Race.run(pubs);
  }

  public static void parallelPubsAndSubs(final ClientFactory factory,
                                         final String pubToken,
                                         final String subToken,
                                         final String category,
                                         final int pubNum,
                                         final int subNum,
                                         final TestRuntimeReport pubReport,
                                         final TestRuntimeReport subReport,
                                         final BufferFinance finance) throws Exception {
    @SuppressWarnings("unchecked") final Callable<Void>[] list =
      (Callable<Void>[]) new Callable<?>[pubNum + subNum];

    for (int i = 0; i < pubNum; i++) {
      list[i] = pub(factory, pubToken + i, pubReport, category, finance.cashier());
    }
    for (int i = pubNum, j = 0; j < subNum; j++, i++) {
      list[i] = sub(factory, subToken + j, category, subReport, finance.accountant());
    }
    Race.run(list);
  }

  public static void parallelSubs(final ClientFactory factory,
                                  final String token,
                                  final String category,
                                  final int num,
                                  final TestRuntimeReport report,
                                  final BufferFinance finance) throws Exception {
    final Callable<Void>[] subs = new Sub[num];
    for (int i = 0; i < subs.length; i++)
      subs[i] = sub(factory, token + i, category, report, finance.accountant());
    Race.run(subs);
  }

  public static void retry(final int retry) {
    TestClient.retry = retry;
  }

  private static void printUsage(final String[] args) {
    System.out.println("Invaild command and arguments : " + Arrays.toString(args));
    System.out.println("Usage : ");
    System.out.println("\tzookeeper  <server-port> <data-dir> <tick-time> <initialized-node-script-file>");
    System.out.println("\tpublisher  <token> <broker-host> <broker-port> <category> <message-size> <parallel> <report-standard> <report-print-period> <times>");
    System.out.println("\tsubscriber <token> <broker-host> <broker-port> <category> <message-size> <parallel> <report-standard> <report-print-period> <times>");
  }

  private static Callable<Void> pub(final ClientFactory factory,
                                    final String token,
                                    final TestRuntimeReport report,
                                    final String category,
                                    final Cashier cashier) {
    return new Pub(factory, token, retry, report, category, cashier);
  }

  private static Callable<Void> sub(final ClientFactory factory,
                                    final String token,
                                    final String category,
                                    final TestRuntimeReport report,
                                    final Accountant accountant) {
    return new Sub(factory, token, retry, report, category, accountant);
  }

  private static final NoneTestRuntimeReport noneTestRuntimeReport = new NoneTestRuntimeReport();

  private static int retry = 0;

  /**
   * {@link Command}
   */
  public enum Command {
    zookeeper {
      @Override
      void startWith(final String... args) throws Exception {
        final int port = Integer.parseInt(args[1]);
        final String dataDir = args[2];
        final int tickTime = Integer.parseInt(args[3]);
        final File scriptFile = new File(args[4]);

        final ZooKeeperServerForTest zkServer = new ZooKeeperServerForTest(port, dataDir, tickTime);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

          @Override
          public void run() {
            zkServer.shutdown();
          }
        }, "shutdown-zookeeper-server"));

        final Thread thread = new Thread(new Runnable() {

          @Override
          public void run() {
            try {
              zkServer.startup();
            } catch (final Exception e) {
              e.printStackTrace();
              System.exit(-1);
            }
          }
        }, "zookeeper-server");
        thread.start();
        
        Thread.sleep(200L); // wait for server started.
        new ZookeeperNodeCreater(("localhost:" + port), (tickTime * 2)).createNodesBy(scriptFile);

        thread.join();
      }
    },
    publisher {
      @Override
      void startWith(final String... args) throws Exception {
        new Driver(args) {

          @Override
          protected void doRun() throws Exception {
            parallelPubs(factory, token, category, num, report, finance);
          }
        }.call();
        
      }
    },
    subscriber {
      @Override
      void startWith(final String... args) throws Exception {
        new Driver(args) {

          @Override
          protected void doRun() throws Exception {
            parallelSubs(factory, token, category, num, report, finance);
          }
        }.call();
        
      }
    };

    abstract void startWith(String... args) throws Exception;

  }

  /**
   * {@link Driver}
   */
  private static abstract class Driver implements Callable<Void> {
    public Driver(final String... args) throws Exception {
      token = args[1];
      category = args[4];
      num = Integer.parseInt(args[6]);
      final String host = args[2];
      final int port = Integer.parseInt(args[3]);
      factory = clientFactory(host, port);

      final long standard = Long.parseLong(args[7]);
      final int printPeriod = Integer.parseInt(args[8]);
      report = newTestRuntimeReport(standard, printPeriod);

      final int size = Integer.parseInt(args[5]);
      final int capacity = Integer.parseInt(args[9]);
      finance = finance(size, capacity);
    }

    @Override
    public final Void call() throws Exception {
      doRun();
      System.out.println(report);
      return null;
    }

    protected abstract void doRun() throws Exception;

    protected final ClientFactory factory;
    protected final TestRuntimeReport report;
    protected final int num;
    protected final String category;
    protected final String token;
    protected final BufferFinance finance;

  }

  /**
   * {@link NoneTestRuntimeReport}
   */
  private static final class NoneTestRuntimeReport implements TestRuntimeReport {
    @Override
    public Counter counterOf(final Exception e) {
      return counter;
    }

    @Override
    public Counter counterOfMessage() {
      return counter;
    }

    @Override
    public Counter counterOfSuccess() {
      return counter;
    }

    @Override
    public void hit(final long elaspe) {}

    private final Counter counter = new NoneCounter();

    private static final class NoneCounter implements Counter {

      @Override
      public void add(final int size) {}

      @Override
      public void increment() {}

    }
  }

}
