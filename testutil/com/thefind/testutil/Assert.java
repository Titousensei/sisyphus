package com.thefind.testutil;

import java.util.*;

import static org.junit.Assert.fail;

/**
 * @author Eric Gaudet
 */
public final class Assert
{
  private Assert() {}

  static <T> boolean contentEquals(Collection<T> expected, Collection<T> actual)
  {
    Map<T, Integer> counts = new HashMap();
    Iterator<T> it1 = expected.iterator();
    while (it1.hasNext()) {
      T elem = it1.next();
      Integer c = counts.get(elem);
      if (c==null) {
        counts.put(elem, Integer.valueOf(1));
      }
      else {
        counts.put(elem, Integer.valueOf(c+1));
      }
    }
    Iterator<T> it2 = actual.iterator();
    while (it2.hasNext()) {
      T elem = it2.next();
      Integer c = counts.get(elem);
      if (c==null) {
        return false;
      }
      else if (c==1) {
        counts.remove(elem);
      }
      else {
        counts.put(elem, Integer.valueOf(c-1));
      }
    }

    return (counts.size()==0);
  }

  /**
   * Collection == Collection
   */
  public static <T> void assertContentEquals(String message, Collection<T> expected, Collection<T> actual)
  {
    if (!contentEquals(expected, actual)) {
      fail (message + " expected: " + expected.toString() + ", was not: " + actual.toString());
    }
  }

  public static <T> void assertContentEquals(Collection<T> expected, Collection<T> actual)
  {
    if (!contentEquals(expected, actual)) {
      fail ("expected: " + expected.toString() + ", was not: " + actual.toString());
    }
  }

  /**
   * Collection == Array
   */
  public static <T> void assertContentEquals(String message, Collection<T> expected, T[] actual)
  {
    if (!contentEquals(expected, Arrays.asList(actual))) {
      fail (message + " expected: " + expected.toString() + ", was not: " + Arrays.toString(actual));
    }
  }

  public static <T> void assertContentEquals(Collection<T> expected, T[] actual)
  {
    if (!contentEquals(expected, Arrays.asList(actual))) {
      fail ("expected: " + expected.toString() + ", was not: " + Arrays.toString(actual));
    }
  }

  /**
   * Array == Collection
   */
  public static <T> void assertContentEquals(String message, T[] expected, Collection<T> actual)
  {
    if (!contentEquals(Arrays.asList(expected), actual)) {
      fail (message + " expected: " + Arrays.toString(expected) + ", was not: " + actual.toString());
    }
  }

  public static <T> void assertContentEquals(T[] expected, Collection<T> actual)
  {
    if (!contentEquals(Arrays.asList(expected), actual)) {
      fail ("expected: " + Arrays.toString(expected) + ", was not: " + actual.toString());
    }
  }

  /**
   * Array == Array
   */
  public static <T> void assertContentEquals(String message, T[] expected, T[] actual)
  {
    if (!contentEquals(Arrays.asList(expected), Arrays.asList(actual))) {
      fail (message + " expected: " + Arrays.toString(expected) + ", was not: " + Arrays.toString(actual));
    }
  }

  public static <T> void assertContentEquals(T[] expected, T[] actual)
  {
    if (!contentEquals(Arrays.asList(expected), Arrays.asList(actual))) {
      fail ("expected: " + Arrays.toString(expected) + ", was not: " + Arrays.toString(actual));
    }
  }
}

