package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfStartsWith
extends TestColumns
{
  public IfStartsWith(String col_fullstr, String col_start)
  { super(new String[] { col_fullstr, col_start }); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  { return that[0].startsWith(that[1]); }

  @Override
  public String toStringWhich()
  { return ".startsWith"; }
}

