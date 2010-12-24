package com.taobao.timetunnel.client;


/**
 * {@link TestRuntimeReport}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-2
 * 
 */
public interface TestRuntimeReport {

  Counter counterOf(Exception e);

  Counter counterOfMessage();

  Counter counterOfSuccess();

  void hit(long elaspe);

}
