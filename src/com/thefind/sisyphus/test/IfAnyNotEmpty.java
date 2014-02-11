package com.thefind.sisyphus.test;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class IfAnyNotEmpty
extends TestColumns
{
  public IfAnyNotEmpty(String... columns)
  { super(columns); }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  {
    for (int i=0 ; i<that.length ; ++i) {
      if (that[i]!=null && !"".equals(that[i])) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toStringWhich()
  { return "?!=null"; }
}

