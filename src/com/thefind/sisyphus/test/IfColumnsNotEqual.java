package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 */
public class IfColumnsNotEqual
extends TestColumns
{
  public IfColumnsNotEqual(String... columns) { super(columns); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  {
    for (int i=1 ; i<that.length ; i++) {
      if (that[i-1]==null) {
        if (that[i]==null) return false;
      }
      else if (that[i-1].equals(that[i])) return false;
    }
    return true;
  }

  @Override
  public String toStringWhich()
  { return "!=*"; }
}

