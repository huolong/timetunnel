package com.taobao.timetunnel.session;

/**
 * @{link Session}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-26
 * 
 */
public interface Session {
  boolean booleanValueOf(Attribute attribute);

  String id();

  int intValueOf(Attribute attribute);

  boolean isInvalid();

  long longValueOf(Attribute attribute);

  String stringValueOf(Attribute attribute);

  Type type();

  void add(InvalidListener listener);

  void remove(InvalidListener listener);

  public interface InvalidListener {
    void onInvalid();
  }
}
