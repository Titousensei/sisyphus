package com.thefind.sisyphus.test;

import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * @author Eric Gaudet
 */
abstract class TestColumns
implements Test
{
  protected final List<String> schema_;

  public TestColumns(String... columns)
  { schema_ = CollectionUtil.asConstList(columns); }

  @Override
  public List<String> getSchemaIn() { return schema_; }

  @Override
  public abstract boolean eval(String[] that)
  throws EvalException;

  public abstract String toStringWhich();

  @Override
  public String toString()
  { return schema_.toString()+toStringWhich(); }
}

