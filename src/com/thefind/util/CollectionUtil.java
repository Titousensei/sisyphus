package com.thefind.util;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class CollectionUtil
{
  public static <T> List<T> asConstList(T... in)
  { return Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(in, in.length))); }

  public static <T> List<T> asConstList(Collection<T> in)
  {
    ArrayList<T> ret = new ArrayList(in);
    ret.trimToSize();
    return Collections.unmodifiableList(ret);
  }

  public static <T> boolean containsAny(List<T> l1, List<T> l2)
  {
    for (T elem : l1) {
      if (l2.contains(l1)) {
        return true;
      }
    }
    return false;
  }

  public static <T> List<T> merge(List<T>... many)
  {
    ArrayList<T> ret = new ArrayList();
    for (List<T> list : many) {
      if (list!=null) {
        for (T val : list) {
          if (!ret.contains(val)) {
            ret.add(val);
          }
        }
      }
    }
    ret.trimToSize();
    return ret;
  }

  public static <T> List<T> merge(T[]... many)
  {
    ArrayList<T> ret = new ArrayList();
    for (T[] list : many) {
      if (list!=null) {
        for (T val : list) {
          if (!ret.contains(val)) {
            ret.add(val);
          }
        }
      }
    }
    ret.trimToSize();
    return ret;
  }
}

