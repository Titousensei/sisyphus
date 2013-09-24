package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
class ActionIfNull
extends ActionBase
{
  protected final String column_;
  protected final boolean is_null_;

  public ActionIfNull(String column, boolean is_null, Action out)
  {
    super(Arrays.asList(column), out);
    column_ = column;
    is_null_ = is_null;
  }

  @Override
  protected String toStringWhich()
  { return "["+column_+ ((is_null_) ? "]==null" : "]!=null"); }

  @Override
  public void use(String[] row)
  throws InterruptException
  {
    String[] entry = getInView(row);

    if (is_null_ == (entry[0]==null || "".equals(entry[0]))) {
      used_++;
      output(row);
    }
  }

  @Override
  public void useParallel(String[] row)
  throws InterruptException
  {
    String[] entry = getInViewParallel(row);

    if (is_null_ == (entry[0]==null || "".equals(entry[0]))) {
      synchronized (this) {
        used_++;
      }
      outputParallel(row);
    }
  }
}

