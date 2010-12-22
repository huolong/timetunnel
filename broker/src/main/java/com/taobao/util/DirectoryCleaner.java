package com.taobao.util;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import com.taobao.util.RecurseTree.Callback;
import com.taobao.util.RecurseTree.IteratorFactory;


/**
 * {@link DirectoryCleaner}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-3
 * 
 */
public final class DirectoryCleaner {

  private DirectoryCleaner() {}

  public static void clean(final File dir) {
    RecurseTree.run(dir, FACTORY, CALLBACK);
  }

  public static void clean(final String dir) {
    clean(new File(dir));
  }

  private final static IteratorFactory<File> FACTORY = new IteratorFactory<File>() {

    @Override
    public Iterator<File> iterator(final File obj) {
      final Iterator<File> empty = RecurseTree.empty();
      return obj.isDirectory() ? Arrays.asList(obj.listFiles()).iterator() : empty;
    }
  };

  private final static Callback<File> CALLBACK = new Callback<File>() {

    @Override
    public void onCallback(final File obj) {
      obj.delete();
    }
  };
}
