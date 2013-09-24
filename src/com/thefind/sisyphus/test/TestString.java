package com.thefind.sisyphus.test;

import java.util.*;

/**
 * @author Eric Gaudet
 */ 
abstract class TestString
implements Test
{
  protected final List<String> schema_;
  protected final String value_;

  public TestString(String column, String value)
  {
    List<String> s = new ArrayList(1);
    s.add(column);
    schema_ = Collections.unmodifiableList(s);
    value_ = value;
  }

  @Override
  public List<String> getSchemaIn() { return schema_; }

  @Override
  public boolean eval(String[] that)
  throws EvalException
  { return eval(that[0]); }

  public abstract boolean eval(String that)
  throws EvalException;

  public abstract String toStringWhich();

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(schema_.toString())
      .append(toStringWhich())
      .append('"')
      .append(value_)
      .append('"');
    return sb.toString();
  }
}

