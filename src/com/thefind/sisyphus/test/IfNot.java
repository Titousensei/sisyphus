package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfNot
extends TestOne
{
  public IfNot(Test test) { super(test); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  { return !test_.eval(that); }

  @Override
  public String toString()
  { return "!("+test_.toString()+")"; }
}

