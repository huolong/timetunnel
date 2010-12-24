package com.taobao.util;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * PropertiesUtils
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-7
 * 
 */
public final class PropertiesHelper {
  public PropertiesHelper(final File file) throws Exception {
    properties = new Properties();
    final FileReader reader = new FileReader(file);
    properties.load(reader);
    reader.close();
  }

  public PropertiesHelper(final Properties properties) {
    this.properties = properties;
  }

  public boolean contains(final Object key) {
    return properties.containsKey(key);
  }

  public <V> V get(final Converter<V> converter, final String key) {
    final String value = properties.getProperty(key).trim();
    if (value == null) throw new IllegalArgumentException(key + " is missing.");
    return converter.convert(value);
  }

  public <V> V get(final Converter<V> converter, final String key, final String defaultValue) {
    if (!properties.containsKey(key)) return converter.convert(defaultValue);
    return converter.convert((properties.getProperty(key).trim()));
  }

  public boolean getBoolean(final String key) {
    final String value = properties.getProperty(key).trim();
    if (value == null) throw new IllegalArgumentException(key + " is missing.");
    return parseBoolean(value);
  }

  public boolean getBoolean(final String key, final boolean defaultValue) {
    if (!properties.containsKey(key)) return defaultValue;
    return parseBoolean(properties.getProperty(key).trim());
  }

  public int getInt(final String key) {
    final String value = properties.getProperty(key).trim();
    if (value == null) throw new IllegalArgumentException(key + " is missing.");
    return parseInt(value);
  }

  public int getInt(final String key, final int defaultValue) {
    if (!properties.containsKey(key)) return defaultValue;
    return parseInt(properties.getProperty(key).trim());
  }

  public String getString(final String key) {
    final String value = properties.getProperty(key).trim();
    if (value == null || value.length() == 0)
      throw new IllegalArgumentException(key + " is missing.");
    return value;
  }

  public String getString(final String key, final String defaultValue) {
    return properties.getProperty(key, defaultValue).trim();
  }

  private final Properties properties;

  public static interface Converter<V> {
    V convert(String value);
  }
}
