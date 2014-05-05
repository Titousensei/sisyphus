package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfEndsWith
extends TestColumns
{
  public IfEndsWith(String col_fullstr, String col_end)
  { super(new String[] { col_fullstr, col_end }); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  { return that[0].endsWith(that[1]); }

  @Override
  public String toStringWhich()
  { return ".endsWith"; }
}

