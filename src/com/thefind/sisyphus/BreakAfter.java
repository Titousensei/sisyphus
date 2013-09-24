package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public final class BreakAfter
implements Action
{
  public final static Action NO_OP = new BreakAfter(null, null);

  private final Action act_;
  private final boolean op_;
  private final InterruptException break_;
  private final String label_;

  public BreakAfter(String label)
  { this(null, label); }

  public BreakAfter(Action act)
  { this(act, null); }

  public BreakAfter(Action act, String label)
  {
    act_   = act;
    label_ = label;
    op_    = (act!=null);
    break_ = new InterruptException(toString());
  }

  @Override
  public boolean open()
  { return (op_) ? act_.open() : true; }

  @Override
  public void close()
  { if (op_) { act_.close(); } }

  @Override
  public List<String> getSchemaIn()
  { return null; }

  @Override
  public List<String> getSchemaOut()
  { return (op_) ? act_.getSchemaOut() : null; }

  @Override
  public void ready(List<String> schema_row)
  { if (op_) { act_.ready(schema_row); } }

  @Override
  public void use(String[] row)
  throws InterruptException
  {
    if (op_) { act_.use(row); }
    throw break_;
  }

  @Override
  public void useParallel(String[] row)
  throws InterruptException
  {
    if (op_) { act_.useParallel(row); }
    throw break_;
  }

  @Override
  public boolean sameAs(long that)
  { return (op_) ? act_.sameAs(that) : false; }

  @Override
  public String toString()
  {
    if (op_) {
      if (label_!=null) {
        return "BreakAfter\""+label_+"\":"+act_.toString();
      }
      else {
        return "BreakAfter:"+act_.toString();
      }
    }
    else if (label_!=null) {
      return "BreakAfter\""+label_+"\"";
    }
    else {
      return "BreakAfter.NO_OP";
    }
  }
}

