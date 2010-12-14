package com.taobao.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * RecurseTree
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-14
 * 
 */
public final class RecurseTree {
  public static final <T> Iterator<T> empty() {
    final Set<T> empty = Collections.emptySet();
    return empty.iterator();
  }

  public static final <T> void run(final T root,
                                   final IteratorFactory<T> factory,
                                   final Callback<T> callback) {

    final Stack<Level<T>> stack = new Stack<Level<T>>();
    stack.push(new Level<T>(root, factory.iterator(root)));
    while (!stack.empty()) {
      final Level<T> level = stack.pop();
      if (level.children.hasNext()) {
        stack.push(level);
        final T child = level.children.next();
        stack.push(new Level<T>(child, factory.iterator(child)));
      } else {
        callback.onCallback(level.parent);
      }
    }
  }

  private RecurseTree() {}

  public interface Callback<T> {
    void onCallback(T obj);
  }

  public interface IteratorFactory<T> {
    Iterator<T> iterator(T obj);
  }

  private static class Level<T> {

    public Level(final T parent, final Iterator<T> children) {
      this.parent = parent;
      this.children = children;
    }

    private final T parent;

    private final Iterator<T> children;

  }
}
