package com.thefind.util;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Gaudet
 */
public class StringUtilTest
{

  // public synchronized static String canonicalize(String in)

  @Test
  public void bytesToLong64Basic()
  {
    byte[] ary = new byte[] {2, 3, 5, 7, 11, 13, 17, 19};
    long expected = 0x020305070B0D1113L;
    long l = StringUtil.bytesToLong64(ary, 0);
    assertEquals("Wrong long "+l, expected, l);
  }

  // public static String join(String[] l, String delim)

  // public static int splitInto(String input, char separator, String[] result, int offset)

  // public static int splitCsvInto(String input, String[] result, int offset)

  // public static String readableTime(long t0)

  // public static String readableSize(long sz)

  // public static String escapeForTSV(String input)

  // public static String unescapeFromTSV(String input)

  // public static int indexOf(String[] ary, String key)

  // public static String replaceAll(String in, char c, String repl)

  // public static String replaceAll(String in, String c, String repl)
}

