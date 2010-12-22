package com.taobao.timetunnel.client;

import com.taobao.timetunnel.thrift.gen.Failure;

/**
 * {@link TestRuntimeReport}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-2
 * 
 */
public interface TestRuntimeReport {

  Counter counterOf(Failure e);

  Counter counterOfMessage();

  Counter counterOfSuccess();

  void hit(long elaspe);

}
