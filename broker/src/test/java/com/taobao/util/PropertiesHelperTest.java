package com.taobao.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;


/**
 * {@link PropertiesHelperTest}
 * @author  <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-7
 * 
 */
public class PropertiesHelperTest {
  @Test
  public void shouldGetActualNotDefault() throws Exception {
    PropertiesHelper helper = new PropertiesHelper(new File("src/test/resources/conf.properties"));
    
    assertThat(helper.getInt("cluster.rebalancePeriod", 60), is(3600));
    assertThat(helper.getString("broker.group", "group"), is("group1"));
  }
}
