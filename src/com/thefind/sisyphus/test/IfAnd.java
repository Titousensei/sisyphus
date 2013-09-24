package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfAnd
extends TestMulti
{
  public IfAnd(Test... test) { super(test); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  {
    for (Test t : test_) {
      if (!t.eval(that)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toStringWhich()
  { return " && "; }
}

