package com.thefind.sisyphus;

import java.util.*;
import com.thefind.sisyphus.test.EvalException;
import com.thefind.sisyphus.test.Test;

/**
 * @author Eric Gaudet
 */
class ActionTest
extends ActionBase
{
  protected final Test tst_;

  public ActionTest(Test tst, Action out)
  {
    super(tst.getSchemaIn(), out);
    tst_ = tst;
  }

  @Override
  protected String toStringWhich()
  { return tst_.toString(); }

  @Override
  public void use(String[] row)
  throws InterruptException
  {
    String[] entry = getInView(row);

    try {
      if (tst_.eval(entry)) {
        used_++;
        output(row);
      }
    }
    catch (EvalException ex) {
      warning_++;
      if (warning_<10) {
        System.err.println("[ActionTest] WARNING - "+tst_.toString()+" : "+ex.getMessage());
      }
    }
  }

  @Override
  public void useParallel(String[] row)
  throws InterruptException
  {
    String[] entry = getInViewParallel(row);

    try {
      if (tst_.eval(entry)) {
        synchronized(this) {
          used_++;
        }
        outputParallel(row);
      }
    }
    catch (EvalException ex) {
      synchronized(this) {
        warning_++;
      }
      if (warning_<10) {
        System.err.println("[ActionTest] WARNING - "+tst_.toString()+" : "+ex.getMessage());
      }
    }
  }
}

