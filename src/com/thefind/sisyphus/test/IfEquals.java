package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfEquals
extends TestString
{
  public IfEquals(String column, String value) { super(column, value); }

  @Override
  public boolean eval(String that)
  throws EvalException
  { return (value_.equals(that)); }

  @Override
  public String toStringWhich()
  { return "=="; }
}

