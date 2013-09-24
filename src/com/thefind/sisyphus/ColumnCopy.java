package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class ColumnCopy
extends Modifier
{
  public ColumnCopy(String outcol, String incol)
  { super(new String[] { incol }, new String[] { outcol }); }

  public void compute(String[] input, String[] result)
  { result[0] = input[0]; }

  @Override
  public String toStringModif()
  { return ""; }
}
