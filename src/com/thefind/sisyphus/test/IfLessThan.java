package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfLessThan
extends TestDouble
{
  public IfLessThan(String column, double value)
  { super(column, value); }

  @Override
  public boolean eval(double that)
  throws EvalException
  { return (that<value_); }

  @Override
  public String toStringWhich()
  { return "<"; }
}

