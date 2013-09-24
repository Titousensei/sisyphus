package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
class ActionIfKeyBinding
extends ActionBase
{
  protected final KeyBinding map_;
  protected final boolean match_;

  public ActionIfKeyBinding(KeyBinding map, boolean found, Action out)
  {
    super(map.getSchemaIn(), out);
    map_ = map;
    match_= found;
  }

  @Override
  protected String toStringWhich()
  { return map_.getSchemaIn()+ (match_ ? "?match" : "?diff"); }

  @Override
  public void use(String[] row)
  throws InterruptException
  {
    String[] entry = getInView(row);

    long k;
    long v;
    try {
      k = Long.parseLong(entry[0]);
      v = Long.parseLong(entry[1]);
    }
    catch (NumberFormatException nfex) {
      warning_++;
      return;
    }
    if (map_.contains(k) && (match_ == (map_.get(k)==v))) {
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
    long v;
    try {
      k = Long.parseLong(entry[0]);
      v = Long.parseLong(entry[1]);
    }
    catch (NumberFormatException nfex) {
      synchronized (this) {
        warning_++;
      }
      return;
    }
    if (map_.contains(k) && (match_ == (map_.get(k)==v))) {
      synchronized (this) {
        used_++;
      }
      outputParallel(row);
    }
  }
}

