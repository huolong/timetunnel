package com.taobao.timetunnel.client;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link BufferFinance}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-8
 * 
 */
public final class BufferFinance {

  /**
   * @param size of buffer, unit is KB.
   * @param capacity of buffer for balance.
   */
  BufferFinance(final int size, final int capacity) {
    this.size = size;
    this.capacity = capacity;
    accountant = newAccountant();
    cashier = newCashier();
  }

  public Accountant accountant() {
    return accountant;
  }

  public Cashier cashier() {
    return cashier;
  }

  private Accountant newAccountant() {
    final BitSet bitSet = new BitSet();
    return new Accountant() {

      @Override
      public boolean balance() {
        return bitSet.cardinality() < capacity;
      }

      @Override
      public void input(final ByteBuffer buffer) {
        final int i = buffer.getInt();
        synchronized (bitSet) {
          if (bitSet.get(i)) throw new IllegalStateException("Duplicate buffer " + i);
          bitSet.set(i);
        }
        while (buffer.hasRemaining()) {
          final int content = buffer.getInt();
          if (content != i)
            throw new IllegalStateException("Invalid buffer content " + content + " is not " + i);
        }
      }
    };
  }

  private Cashier newCashier() {
    final AtomicInteger count = new AtomicInteger();
    return new Cashier() {

      @Override
      public boolean balance() {
        return count.get() < capacity;
      }

      @Override
      public ByteBuffer output() {
        final int i = count.getAndIncrement();
        final ByteBuffer buffer = ByteBuffer.allocate(size * 1024);
        while (buffer.hasRemaining()) {
          buffer.putInt(i);
        }
        return (ByteBuffer) buffer.flip();
      }
    };
  }

  private final int size;
  private final int capacity;
  private final Accountant accountant;
  private final Cashier cashier;

  /**
   * {@link Accountant}
   */
  public interface Accountant {
    boolean balance();

    void input(ByteBuffer buffer);
  }

  /**
   * {@link Cashier}
   */
  public interface Cashier {
    boolean balance();

    ByteBuffer output();
  }
}
