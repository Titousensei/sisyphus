package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * @author Eric Gaudet
 */
public abstract class Input
{
  protected final List<String> schema_;
  protected String[] container_;
  protected int sz_;
  protected int row_sz_;

  protected int lines_;
  protected int too_short_;
  protected int too_long_;

  protected SchemaAdapter adapter_;

  protected Input(String[] schema)
  { this(CollectionUtil.asConstList(schema)); }

  protected Input(List<String> schema)
  {
    schema_  = schema;
    sz_ = schema_.size();
  }

  public List<String> getSchemaOut()
  { return schema_; }

  public void ready(List<String> schema_row)
  {
    adapter_ = new SchemaAdapter(schema_);
    adapter_.setSchemaIn(schema_row);
    container_ = new String[schema_row.size()];
    lines_ = 0;
    too_short_ = 0;
    too_long_  = 0;
    row_sz_ = -1;
  }

  public String[] getRow()
  {
    if (container_!=null) {
      adapter_.clearContainer();
      row_sz_ = readRow(adapter_.getContainer());
      if (row_sz_>sz_) {
        too_long_++;
      }
      else if (row_sz_<sz_ && row_sz_>=0) {
        too_short_++;
      }
      if (row_sz_!=-1) {
        Arrays.fill(container_, null);
        adapter_.applyView(container_);
        lines_++;
        return container_;
      }
      container_ = null;
    }
    else {
      System.err.println("[Input] ERROR - Not Ready: "+toString());
    }
    return null;
  }

  public String[] getCurrentRow()
  { return container_; }

  public boolean isRowTooLong()
  { return (row_sz_>sz_); }

  public boolean isRowTooShort()
  { return (row_sz_<sz_ && row_sz_>0); }

  public int getRowSize()
  { return row_sz_; }

  public boolean isRowEmpty()
  { return (row_sz_==0); }

  public int getLines()
  { return lines_; }

  public abstract boolean open();

  public abstract void close();

  /**
   * @return number of columns in the row, or -1 for EOF
   */
  protected abstract int readRow(String[] result);

  protected abstract String toStringWhich();

  protected abstract long getInternalHashCode();

  public boolean sameAs(Action act)
  { return act.sameAs(getInternalHashCode()); }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName())
      .append('{')
      .append(toStringWhich())
      .append(" -> ")
      .append(getSchemaOut());
    if (too_long_>0) {
      sb.append(", too_long: ")
        .append(String.format("%,d", too_long_))
        .append(" rows");
    }
    if (too_short_>0) {
      sb.append(", too_short: ")
        .append(String.format("%,d", too_short_))
        .append(" rows");
    }
    sb.append('}');
    return sb.toString();
  }

  public static List<String> mergedSchema(Input... inputs)
  {
    ArrayList<String> ret = new ArrayList();
    for (Input in : inputs) {
      for (String col : in.getSchemaOut()) {
        if (!ret.contains(col)) {
          ret.add(col);
        }
      }
    }
    ret.trimToSize();
    return Collections.unmodifiableList(ret);
  }
}

