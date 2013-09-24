package com.thefind.sisyphus;

import java.util.Arrays;

/**
 * @author Eric Gaudet
 */
public class OutputKeyDouble
extends Output
{
  protected final KeyDouble kdouble_;

  public OutputKeyDouble(KeyDouble kdouble)
  {
    super(kdouble.getSchemaIn());
    kdouble_ = kdouble;
  }

  public OutputKeyDouble(KeyDouble kdouble, String... schema)
  {
    super(schema);
    kdouble_ = kdouble;
  }

  @Override
  protected void append(String[] values)
  {
    try {
      long   key = Long.parseLong(values[0]);
      double val = Double.parseDouble(values[1]);
      if (!Double.isNaN(val)) {
        kdouble_.put(key, val);
      }
    }
    catch (NumberFormatException nfex) {
      warnings_ ++;
      if (warnings_<10) {
        System.out.println("[OutputKeyDouble] WARNING - NumberFormatException "
            + Arrays.toString(values) + " in " + kdouble_.toString());
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
  { return (that==kdouble_.hashCode()); }

  @Override
  protected String toStringWhich()
  { return kdouble_.toString(); }
}

