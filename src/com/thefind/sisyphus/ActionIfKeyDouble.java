package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
class ActionIfKeyDouble
extends ActionBase
{
  protected final KeyDouble double_;
  protected final boolean match_;

  public ActionIfKeyDouble(KeyDouble dble, boolean found, Action out)
  {
    super(dble.getSchemaIn(), out);
    double_ = dble;
    match_= found;
  }

  @Override
  protected String toStringWhich()
  { return double_.getSchemaIn()+ (match_ ? "?match" : "?diff"); }

  @Override
  public void use(String[] row)
  throws InterruptException
  {
    String[] entry = getInView(row);

    long k;
    double v;
    try {
      k = Long.parseLong(entry[0]);
      v = Double.parseDouble(entry[1]);
    }
    catch (NumberFormatException nfex) {
      warning_++;
      return;
    }
    if (double_.contains(k) && (match_ == (double_.get(k)==v))) {
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
    double v;
    try {
      k = Long.parseLong(entry[0]);
      v = Double.parseDouble(entry[1]);
    }
    catch (NumberFormatException nfex) {
      synchronized (this) {
        warning_++;
      }
      return;
    }
    if (double_.contains(k) && (match_ == (double_.get(k)==v))) {
      synchronized (this) {
        used_++;
      }
      outputParallel(row);
    }
  }
}

