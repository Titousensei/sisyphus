package com.thefind.sisyphus;

import java.util.Arrays;

/**
 * @author Eric Gaudet
 */
public class OutputKeyString
extends Output
{
  protected final KeyString kstring_;

  public OutputKeyString(KeyString kstring)
  {
    super(kstring.getSchemaIn());
    kstring_ = kstring;
  }

  public OutputKeyString(KeyString kstring, String... schema)
  {
    super(schema);
    kstring_ = kstring;
  }

  @Override
  protected void append(String[] values)
  {
    try {
      long key = Long.parseLong(values[0]);
      kstring_.put(key, values[1]);
    }
    catch (NumberFormatException nfex) {
      warnings_ ++;
      if (warnings_<10) {
        System.out.println("[OutputKeyString] WARNING - NumberFormatException "
            + Arrays.toString(values) + " in " + kstring_.toString());
      }
      return;
    }
  }

  @Override
  public boolean open() { return true; }

  @Override
  public void close() {}

  @Override
  public boolean sameAs(long that)
  { return (that==kstring_.hashCode()); }

  @Override
  protected String toStringWhich()
  { return kstring_.toString(); }
}

