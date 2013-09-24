package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class KeyDoubleSum
extends OutputKeyDouble
{
  protected String column_;

  public KeyDoubleSum(KeyDouble kdbl, String column)
  {
    super(kdbl, new String[] { kdbl.getSchemaIn().get(0), column });
    column_ = column;
  }

  @Override
  public void append(String[] that)
  {
    try {
      long key = Long.parseLong(that[0]);
      double value = Double.parseDouble(that[1]);
      if (!Double.isNaN(value)) {
        kdouble_.increment(key, value);
      }
    }
    catch (NumberFormatException nfex) {
      return;
    }
  }

  @Override
  public String toStringWhich()
  {
    List<String> sch = kdouble_.getSchemaIn();
    return "["+sch.get(0)+", "+sch.get(1)+"+=["+column_+"]]";
  }
}

