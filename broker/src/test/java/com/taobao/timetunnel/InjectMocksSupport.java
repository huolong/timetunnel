package com.taobao.timetunnel;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * BaseTestCase
 * @author  <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
public abstract class InjectMocksSupport {
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }
}
