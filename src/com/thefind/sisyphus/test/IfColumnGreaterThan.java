package com.thefind.sisyphus.test;

/**
 * @author Eric Gaudet
 * @author Qin Wang
 */
public class IfColumnGreaterThan
extends TestColumns
{
  public IfColumnGreaterThan(String col0, String col1)
  { super(new String[] { col0, col1 }); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  {
    try {
      double c0 = (that[0]!=null) ? Double.parseDouble(that[0]) : 0.0;
      double c1 = (that[1]!=null) ? Double.parseDouble(that[1]) : 0.0;
      return (c0>c1);
    }
    catch (NumberFormatException nfex) {
      throw new EvalException("Cannot parseDouble(\""+that[0]+"\", \""+that[1]+"\")");
    }
  }

  @Override
  public String toStringWhich()
  { return ">"; }
}

