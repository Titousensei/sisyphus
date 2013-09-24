package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
class ActionIfKey
extends ActionBase
{
  protected final Key key_;
  protected final boolean found_;

  public ActionIfKey(Key key, boolean found, Action out)
  {
    super(key.getSchemaIn().subList(0,1), out);
    key_ = key;
    found_ = found;
  }

  @Override
  protected String toStringWhich()
  { return key_.getSchemaIn()+ (found_ ? "?found" : "?miss"); }

  @Override
  public void use(String[] row)
  throws InterruptException
  {
    String[] entry = getInView(row);

    long k;
    try {
      k = Long.parseLong(entry[0]);
    }
    catch (NumberFormatException nfex) {
      warning_++;
      return;
    }

    if (found_ == key_.contains(k)) {
      used_++;
      output(row);
    }
  }

  @Override
  public void useParallel(String[] row)
  throws InterruptException
  {
    String[] entry = getInViewParallel(row);

    long k;
    try {
      k = Long.parseLong(entry[0]);
    }
    catch (NumberFormatException nfex) {
      synchronized (this) {
        warning_++;
      }
      return;
    }

    if (found_ == key_.contains(k)) {
      synchronized (this) {
        used_++;
      }
      outputParallel(row);
    }
  }
}

