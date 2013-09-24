package com.thefind.sisyphus.test;

import java.util.*;

/**
 * @author Eric Gaudet
 */
abstract class TestMulti
implements Test
{
  protected final List<String> schema_;
  protected final Test[] test_;

  public TestMulti(Test... test)
  {
    test_ = test;

    Set<String> sch = new HashSet();
    for (Test t : test) {
      for (String s : t.getSchemaIn()) {
        sch.add(s);
      }
    }
    schema_ = Collections.unmodifiableList(new ArrayList(sch));
  }

  @Override
  public List<String> getSchemaIn() { return schema_; }

  @Override
  public abstract boolean eval(String[] that)
  throws EvalException;

  public abstract String toStringWhich();

  @Override
  public String toString()
  {
    StringBuilder sb = null;
    for (Test t : test_) {
      if (sb==null) {
        sb = new StringBuilder();
      }
      else {
        sb.append(toStringWhich());
      }
      sb.append('(')
        .append(t.toString())
        .append(')');
    }

    return sb.toString();
  }
}

