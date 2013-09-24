package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class ColumnTrim
extends Modifier
{
  public ColumnTrim(String outcol, String incol)
  { super(new String[] { incol }, new String[] { outcol }); }

  public void compute(String[] input, String[] result)
  { result[0] = input[0].trim(); }

  @Override
  public String toStringModif()
  { return ".trim()"; }
}

