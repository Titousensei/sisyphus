package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * @author Eric Gaudet
 */
abstract class ActionBase
implements Action
{
  protected final SchemaAdapter in_adapter_;
  protected final Action out_;

  protected int used_ = 0;
  protected int warning_ = 0;

  public ActionBase(List<String> tst_schema, Action out)
  {
    in_adapter_ = new SchemaAdapter(tst_schema);
    out_ = out;
  }

  @Override
  public boolean open()
  { return out_.open(); }

  @Override
  public void close()
  { out_.close(); }

  @Override
  public List<String> getSchemaIn()
  { return CollectionUtil.merge(in_adapter_.getSchemaOut(), out_.getSchemaIn()); }

  @Override
  public List<String> getSchemaOut()
  { return out_.getSchemaOut(); }

  @Override
  public void ready(List<String> schema_row)
  {
    in_adapter_.setSchemaIn(schema_row);
    out_.ready(schema_row);
  }

  public void output(String[] row)
  throws InterruptException
  { out_.use(row); }

  public void outputParallel(String[] row)
  throws InterruptException
  { out_.useParallel(row); }

  @Override
  public boolean sameAs(long that)
  { return out_.sameAs(that); }

  protected abstract String toStringWhich();

  protected String[] getInView(String[] row)
  { return in_adapter_.getView(row); }

  protected synchronized String[] getInViewParallel(String[] row)
  {
    String[] in_copy  = in_adapter_.newContainer();
    in_adapter_.getView(in_copy, row);
    return in_copy;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName()+"{"+toStringWhich()+" -> "+out_.toString()
               +", "+String.format("%,d", used_)+" used"
               +((warning_>0)
                 ? ", "+String.format("%,d", warning_)+" warnings}"
                 : "}");
  }
}

