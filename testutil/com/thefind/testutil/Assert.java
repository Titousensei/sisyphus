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
    Iterator<T> it1 = expected.iterator();
    while (it1.hasNext()) {
      if (!actual.contains(it1.next())) { return false; }
    }
    Iterator<T> it2 = actual.iterator();
    while (it2.hasNext()) {
      if (!expected.contains(it2.next())) { return false; }
    }
    return true;
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

