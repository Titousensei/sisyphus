package com.thefind.sisyphus;

import java.util.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public class ColumnsHashLong
extends Modifier
{
  private static final ThreadLocal<MessageDigest> MD5 = new ThreadLocal() {
      @Override protected MessageDigest initialValue()
      {
        try {
          return MessageDigest.getInstance("md5");
        }
        catch (NoSuchAlgorithmException ex) {
          System.err.println("[ColumnsHashLong.initialValue] ERROR - No such algorithm: md5");
          return null;
        }
     }
  };

  public ColumnsHashLong(String outcol, String... incol)
  {
    super(incol, new String[] { outcol });
  }

  @Override
  public void compute(String[] input, String[] result)
  {
    String combi = StringUtil.join(input, "\t");
    result[0] = Long.toString(hash(combi));
  }

  public static long hash(String s)
  {
    byte[] bary = MD5.get().digest(s.getBytes(FileWriter.DEFAULT_CHARSET));
    return bytesToLong64(bary);
  }

  private static long bytesToLong64(byte[] ary)
  {
    // this follows network byte order (big endian)
    return ((((long) ary[0] & 0xff) << 56) |
            (((long) ary[1] & 0xff) << 48) |
            (((long) ary[2] & 0xff) << 40) |
            (((long) ary[3] & 0xff) << 32) |
            (((long) ary[4] & 0xff) << 24) |
            (((long) ary[5] & 0xff) << 16) |
            (((long) ary[6] & 0xff) << 8) |
            (((long) ary[7] & 0xff)));
  }

  @Override
  public String toStringModif()
  { return ".md5().toLong64()"; }
}
