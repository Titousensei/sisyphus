package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * @author Eric Gaudet
 * @author Rajkumar Ponnusamy
 */
public abstract class Output
implements Action
{
  protected final SchemaAdapter adapter_;
  protected final List<String> schema_;
  private   int lines_;
  protected int warnings_;
  protected boolean verbose_ = true;

  public boolean debug_ = false;

  protected Output(String[] schema)
  { this(CollectionUtil.asConstList(schema)); }

  protected Output(List<String> schema)
  {
    schema_  = schema;
    adapter_ = new SchemaAdapter(schema_);
    lines_   = 0;
    warnings_ = 0;
  }

  public void setVerbose(boolean val)
  { verbose_ = val; }

  @Override
  public List<String> getSchemaIn()
  { return schema_; }

  @Override
  public List<String> getSchemaOut()
  { return null; }

  @Override
  public final void ready(List<String> schema_row)
  {
    adapter_.setSchemaIn(schema_row);
    readySchema(schema_);
  }

  @Override
  public void use(String[] row)
  {
    append(adapter_.getView(row));
    lines_++;
  }

  @Override
  public void useParallel(String[] row)
  {
    String[] in_copy  = adapter_.newContainer();
    adapter_.getView(in_copy, row);
    synchronized (this) {
      append(in_copy);
      lines_++;
    }
  }

  public void useDirect(String[] cols)
  {
    append(cols);
    lines_++;
  }

  public int getLines()
  { return lines_; }

  @Override
  public abstract boolean open();

  @Override
  public abstract void close();

  protected abstract void append(String[] values);

  protected abstract String toStringWhich();

  protected void readySchema(List<String> schema_row) {}

  @Override
  public abstract boolean sameAs(long that);

  @Override
  public String toString()
  {
    String ret = getClass().getSimpleName()+"{"+getSchemaIn()+" -> "+toStringWhich()
                 +" +"+String.format("%,d", lines_)+" rows";
    if (warnings_>0) {
      ret += ", "+String.format("%,d", warnings_)+" warnings";
    }
    return ret+"}";
  }
}
