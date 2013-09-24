package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfOr
extends TestMulti
{
  public IfOr(Test... test) { super(test); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  {
    for (Test t : test_) {
      if (t.eval(that)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toStringWhich()
  { return " && "; }
}

