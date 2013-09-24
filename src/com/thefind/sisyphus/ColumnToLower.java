package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
public class ColumnToLower
extends Modifier
{
  public ColumnToLower(String incol, String outcol)
  { super(new String[] { incol }, new String[] { outcol }); }

  public void compute(String[] input, String[] result)
  { result[0] = input[0].toLowerCase(); }

  @Override
  public String toStringModif()
  { return ".toLowerCase()"; }
}
