package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class ColumnSwap
extends Modifier
{
  public ColumnSwap(String outcol, String incol)
  { super(new String[] { incol }, new String[] { outcol }); }

  public void compute(String[] input, String[] result)
  {
    String tmp = result[0];
    result[0]  = input[0];
    input[0]   = tmp;
  }

  @Override
  public String toString()
  { return getClass().getSimpleName()+"{ "+getSchemaIn()+" <-> "+getSchemaOut()+"}"; }
}
