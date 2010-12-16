package com.taobao.util;

import static java.text.MessageFormat.format;

/**
 * {@link MemoryMonitor}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-12
 * 
 */
public final class MemoryMonitor {
  public MemoryMonitor(final long shortage, final long abundant) {
    if (shortage >= abundant || abundant >= max())
      throw new IllegalArgumentException(format("Invaild shortage[{0}], abundant[{1}]",
                                                shortage,
                                                abundant));
    this.shortage = shortage;
    this.abundant = abundant;
  }

  public boolean isAbundant() {
    return free() >= abundant;
  }

  public boolean isShort() {
    return free() <= shortage;
  }

  public static long free() {
    return Runtime.getRuntime().freeMemory();
  }

  public static long max() {
    return Runtime.getRuntime().maxMemory();
  }

  private final long shortage;

  private final long abundant;

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MemoryMonitor [shortage=")
           .append(shortage)
           .append(", abundant=")
           .append(abundant)
           .append(", free=")
           .append(free())
           .append(", max=")
           .append(max())
           .append("]");
    return builder.toString();
  }

}
