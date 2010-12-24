package com.taobao.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * {@link SizeConverterTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-23
 * 
 */
public class SizeConverterTest {
  @Test
  public void MB() throws Exception {
    assertThat(new SizeConverter().convert("1M"), is(1 << 20));
  }

  @Test(expected=IllegalArgumentException.class)
  public void invalidMB() throws Exception {
    new SizeConverter().convert("20000M");
  }

  @Test
  public void KB() throws Exception {
    assertThat(new SizeConverter().convert("16K"), is((1 << 10) * 16));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void invalidGB() throws Exception {
    new SizeConverter().convert("1G");
  }

  @Test(expected=IllegalArgumentException.class)
  public void noUnit() throws Exception {
    new SizeConverter().convert("100");
  }
}
