package com.taobao.util;

import com.taobao.util.PropertiesHelper.Converter;

/**
 * {@link SizeConverter}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-23
 * 
 */
public final class SizeConverter implements Converter<Integer> {

  @Override
  public Integer convert(final String value) {
    final int number = Integer.parseInt(value.substring(0, value.length() - 1));
    final int bytes =
      Unit.valueOf(value.substring(value.length() - 1).toUpperCase()).toBytes(number);
    if (bytes < 0) throw new IllegalArgumentException(value + " is out of bound.");
    return bytes;
  }

  enum Unit {
    M {
      @Override
      int toBytes(final int value) {
        return (1 << 20) * value;
      }
    },
    K {
      @Override
      int toBytes(final int value) {
        return (1 << 10) * value;
      }
    };

    abstract int toBytes(int value);
  }
}
