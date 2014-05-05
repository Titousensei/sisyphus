package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class HasSubstring
extends TestColumns
{
  public HasSubstring(String col_fullstr, String col_substr)
  { super(new String[] { col_fullstr, col_substr }); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  { return (that[0].indexOf(that[1]) != -1); }

  @Override
  public String toStringWhich()
  { return ".contains"; }
}

