package com.thefind.sisyphus.test;

import java.util.*;

/**
 * @author Eric Gaudet
 */
abstract class TestOne
implements Test
{
  protected final Test test_;

  public TestOne(Test test)
  { test_ = test; }

  @Override
  public List<String> getSchemaIn() { return test_.getSchemaIn(); }

  @Override
  public abstract boolean eval(String[] that)
  throws EvalException;
}

