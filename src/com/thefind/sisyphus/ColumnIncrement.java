package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class ColumnIncrement
extends Modifier
{
  protected final int increment_;

  public ColumnIncrement(String outcol, int increment)
  {
    super(new String[] { outcol });
    increment_ = increment;
  }

  @Override
  public void compute(String[] input, String[] result)
  {
    try {
      int value = Integer.parseInt(input[0]);
      result[0] = Integer.toString(value + increment_);
    }
    catch (NumberFormatException nfex) {
      result[0] = null;
    }
  }
  @Override
  public String toStringModif()
  { return ((increment_>=0) ? " +" : " " ) + increment_; }
}
