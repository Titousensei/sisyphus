package com.thefind.util;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Eric Gaudet
 */
public class StringUtil
{
  public final static int NUM_MILLIS_IN_SECOND = 1000;
  public final static int NUM_MILLIS_IN_MINUTE = 60*NUM_MILLIS_IN_SECOND;
  public final static int NUM_MILLIS_IN_HOUR   = 60*NUM_MILLIS_IN_MINUTE;
  public final static int NUM_MILLIS_IN_DAY    = 24*NUM_MILLIS_IN_HOUR;

  public final static String[] SIZE_UNITS = new String[] {
    "B", "KB", "MB", "GB", "TB"
  };

  // usage: StringUtils.SIMPLE_DATE.get().format(new Date())
  public final static ThreadLocal<SimpleDateFormat> SIMPLE_DATE = new ThreadLocal() {
      @Override protected SimpleDateFormat initialValue()
      { return new SimpleDateFormat("yyyy-MM-dd"); }
  };
  public final static ThreadLocal<SimpleDateFormat> SIMPLE_TIMESTAMP = new ThreadLocal() {
      @Override protected SimpleDateFormat initialValue()
      { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); }
  };
  public final static ThreadLocal<SimpleDateFormat> SIMPLE_TIMESTAMP_SUFFIX = new ThreadLocal() {
      @Override protected SimpleDateFormat initialValue()
      { return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); }
  };

  private static Map<String, WeakReference<String>> _canonicalStrings = new WeakHashMap();

  /**
   * Canonicalize a string.
   *
   * Several version of the same string can be held in memory by different
   * String objects, for instance if created from an InputStream, or from a
   * substring of a large document.
   *
   * When canonicalized, only one version of the string is used, thus saving
   * memory. The canonical version is stored in a WeakReference object and will
   * be garbage collected when the String is not used anymore.
   *
   * Canonicalize String objects can be compared with ==, which is faster than
   * String.equals().
   *
   * @return the canonical reference of a String.
   */
  public synchronized static String canonicalize(String in)
  {
    WeakReference<String> ref = _canonicalStrings.get(in);
    String str = (ref != null) ? ref.get() : null;
    if (str != null) {
      return str;
    }

    // a substring could reference a much larger shared buffer;
    // creating a new string will trim the buffer
    in = new String(in);
    _canonicalStrings.put(in, new WeakReference(in));
    return in;
  }

  /**
   *
   */
  private static MessageDigest newMd5()
  {
    try {
      return MessageDigest.getInstance("md5");
    }
    catch (NoSuchAlgorithmException ex) {
      return null;
    }
  }

  /**
   *
   */
  public static String join(String[] l, String delim)
  {
    StringBuilder result = null;
    for (int i=0 ; i<l.length ; i++) {
      if (result==null) {
        result = new StringBuilder();
      }
      else {
        result.append(delim);
      }
      result.append(l[i]);
    }
    return result.toString();
  }

  /**
   *
   */
  public static int splitInto(String input, char separator, String[] result, int offset)
  {
    int j = offset;
    int start = 0;
    int end = input.indexOf(separator);
    while (end!=-1) {
      if (j<result.length) result[j] = input.substring(start, end);
      start = end + 1;
      end = input.indexOf(separator, start);
      j ++;
    }
    if (j<result.length) result[j] = input.substring(start);
    j ++;

    int i = j;
    while (i<result.length) {
      result[i++] = null;
    }

    return j;
  }

  /**
   *
   */
  public static String readableTime(long t0)
  {
    if (t0==0) {
      return "0s";
    }
    boolean started = false;
    StringBuilder sb = new StringBuilder();
    long days = t0 / NUM_MILLIS_IN_DAY;
    t0 = t0 % NUM_MILLIS_IN_DAY;
    if (days>0) {
      sb.append(days).append("d ");
      started = true;
    }
    long hours = t0 / NUM_MILLIS_IN_HOUR;
    t0 = t0 % NUM_MILLIS_IN_HOUR;
    if (hours>0 || (started && t0>0)) {
      sb.append(hours).append("h ");
      started = true;
    }
    long minutes = t0 / NUM_MILLIS_IN_MINUTE;
    t0 = t0 % NUM_MILLIS_IN_MINUTE;
    if (minutes>0 || (started && t0>0)) {
      sb.append(minutes).append("m ");
      started = true;
    }
    long seconds = t0 / NUM_MILLIS_IN_SECOND;
    t0 = t0 % NUM_MILLIS_IN_SECOND;
    if (seconds>0 || (started && t0>0)) {
      sb.append(seconds).append("s ");
      started = true;
    }
    if (t0>0) {
      sb.append(t0).append("ms");
      started = true;
    }
    return sb.toString().trim();
  }

  public static String readableSize(long sz)
  {
    double ret = sz;
    if (ret>=1024.0) {
      for (int i = 1 ; i<SIZE_UNITS.length ; i++) {
        ret = ret / 1024.0;
        if (ret<1024.0) {
          ret = Math.round(ret*100);
          return (ret/100)+SIZE_UNITS[i];
        }
      }
    }
    return String.format("%,d", sz)+SIZE_UNITS[0];
  }

  public static String escapeForTSV(String input)
  {
    input = replaceAll(input, '\\', "\\\\");
    input = replaceAll(input, '\n', "\\n");
    input = replaceAll(input, '\r', "\\r");
    input = replaceAll(input, '\t', "\\t");
    return input;
  }

  public static String unescapeFromTSV(String input)
  {
    input = replaceAll(input, "\\t", "\t");
    input = replaceAll(input, "\\r", "\r");
    input = replaceAll(input, "\\n", "\n");
    input = replaceAll(input, "\\\\", "\\");
    return input;
  }

  public static int indexOf(String[] ary, String key)
  {
    for (int i=0 ; i<ary.length ; i++) {
      if (key.equals(ary[i])) return i;
    }
    return -1;
  }

  public static String replaceAll(String in, char c, String repl)
  {
    int cut1 = in.indexOf(c);
    if ((cut1>=0)) {
      int l = in.length();
      StringBuilder sb = new StringBuilder();
      int cut0 = 0;
      while (cut1>=0) {
        sb.append(in.substring(cut0, cut1))
          .append(repl);
        cut0 = cut1 + 1;
        if (cut0>=l) {
          break;
        }
        cut1 = in.indexOf(c, cut0);
      }

      if (cut0<l) {
        sb.append(in.substring(cut0, l));
      }
      return sb.toString();
    }
    else {
      return in;
    }
  }

  public static String replaceAll(String in, String c, String repl)
  {
    int s = c.length();
    int cut1 = in.indexOf(c);
    if ((cut1>=0)) {
      int l = in.length();
      StringBuilder sb = new StringBuilder();
      int cut0 = 0;
      while (cut1>=0) {
        sb.append(in.substring(cut0, cut1))
          .append(repl);
        cut0 = cut1 + s;
        if (cut0>=l) {
          break;
        }
        cut1 = in.indexOf(c, cut0);
      }

      if (cut0<l) {
        sb.append(in.substring(cut0, l));
      }
      return sb.toString();
    }
    else {
      return in;
    }
  }
}

