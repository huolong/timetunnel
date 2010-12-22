package com.taobao.timetunnel.reliable;

import java.nio.ByteBuffer;

import com.taobao.timetunnel.swap.Point;
import com.taobao.util.Bytes;

/**
 * {@link FakePoint}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
final class FakePoint implements Point<ByteBuffer> {

  public static final Point<ByteBuffer> point(int i) {
    return new FakePoint(i);
  }

  private Integer i;

  private FakePoint(int i) {
    this.i = i;
  }

  @Override
  public ByteBuffer get() {
    return Bytes.toBuffer(i);
  }

  @Override
  public void clear() {}

}
