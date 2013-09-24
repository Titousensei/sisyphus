package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.MurmurHash3;

/**
 * @author Eric Gaudet
 */
public class ColumnsHashInt
extends Modifier
{
  public final static int HASH_SEED = 0x1234ABCD;
  public final static byte[] NULL_BYTES = new byte[] { 0 };

  public final int seed_;

  public ColumnsHashInt(String outcol, int seed, String... incol)
  {
    super(incol, new String[] { outcol });
    seed_ = seed;
  }

  public ColumnsHashInt(String outcol, String... incol)
  { this(outcol, HASH_SEED, incol); }

  public void compute(String[] input, String[] result)
  {
    int hash = seed_;

    for (int i = 0 ; i<input.length ; i++) {
      String str = input[i];
      if (str!=null) {
        byte[] b = str.getBytes(FileWriter.DEFAULT_CHARSET);
        hash = MurmurHash3.calc(b, 0, b.length, hash);
      }
      else {
        hash = MurmurHash3.calc(NULL_BYTES, 0, 1, hash);
      }
    }

    result[0] = String.valueOf(hash);
  }

  @Override
  public String toStringModif()
  { return ".murmurHash3()"; }
}
