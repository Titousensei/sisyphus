package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public final class KeyDoubleSetter
extends OutputKeyDouble
{
  private final double value_;

  public KeyDoubleSetter(KeyDouble kdouble, double value)
  {
    super(kdouble, kdouble.getSchemaIn().get(0));
    value_ = value;
  }

  @Override
  public void append(String[] column)
  {
    if (column.length>0) {
      kdouble_.put(column[0], value_);
    }
  }

  @Override
  public String toStringWhich()
  { return "["+schema_.get(0)+","+value_+"]"; }
}

