package com.thefind.sisyphus.test;

import java.util.*;

/**
 * @author Eric Gaudet
 */
abstract class TestDouble
implements Test
{
  protected final List<String> schema_;
  protected final double value_;

  public TestDouble(String column, double value)
  {
    List<String> s = new ArrayList(1);
    s.add(column);
    schema_ = Collections.unmodifiableList(s);
    value_ = value;
  }

  @Override
  public List<String> getSchemaIn() { return schema_; }

  @Override
  public boolean eval(String[] entry)
  throws EvalException
  {
    try {
      return eval(Double.parseDouble(entry[0]));
    }
    catch (NumberFormatException nfex) {
      throw new EvalException("Cannot parseDouble(\""+entry[0]+"\")");
    }
  }

  public abstract boolean eval(double that)
  throws EvalException;

  public abstract String toStringWhich();

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(schema_.toString())
      .append(toStringWhich())
      .append(value_);
    return sb.toString();
  }
}

