package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class OutputKey
extends Output
{
  protected final Key key_;

  public OutputKey(Key key)
  {
    super(key.getSchemaIn().subList(0,1));
    key_ = key;
  }

  @Override
  protected void append(String[] values)
  {
    try {
      long l = Long.parseLong(values[0]);
      key_.add(l);
    }
    catch (NumberFormatException nfex) {
      warnings_ ++;
      if (warnings_<10) {
        System.out.println("[OutputKey] WARNING - NumberFormatException "
            + Arrays.toString(values) + " in " + key_.toString());
      }
    }
  }

  @Override
  public boolean open() { return true; }

  @Override
  public void close() {}

  @Override
  public boolean sameAs(long that)
  { return (that==key_.hashCode()); }

  @Override
  protected String toStringWhich()
  { return key_.toString(); }
}

