package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public class InputSelfJoinSorted
extends Input
{
  protected final Input input_;

  protected final SchemaAdapter out_adapter_;
  protected final RowComparator next_comparator_;
  protected final long col_join_;

  protected String[] next_row_;

  protected List<String[]> buffer_;
  protected int buffer_l_;
  protected int buffer_r_;

  public InputSelfJoinSorted(Input input, String... join_schema)
  {
    super(selfJoinSchema(input.getSchemaOut(), join_schema));

    input_  = input;
    buffer_ = null;

    out_adapter_     = new SchemaAdapter(schema_);
    next_comparator_ = new RowComparator(join_schema);

    List<String> in_schema = input.getSchemaOut();
    long col_join = 0;
    for (int i=0 ; i<in_schema.size() ; i++) {
      if (StringUtil.indexOf(join_schema, in_schema.get(i))!=-1) {
        col_join |= (1<<i);
      }
    }
    col_join_ = col_join;
  }

  private static List<String> selfJoinSchema(List<String> in, String[] join_schema)
  {
    int sz = in.size();
    ArrayList<String> ret = new ArrayList(sz*2);
    for (int i=0 ; i<sz ; i++) {
      String col = in.get(i);
      ret.add(col);
      if (StringUtil.indexOf(join_schema, col)==-1) {
        ret.add("r."+col);
      }
    }
    return ret;
  }

  @Override
  public boolean open()
  {
    if (!input_.open()) {
      System.err.println("[InputSelfJoinSorted] ERROR - Could not open "+input_);
      return false;
    }

    return true;
  }

  @Override
  public void ready(List<String> schema_row)
  {
    super.ready(schema_row);

    input_.ready(input_.getSchemaOut());
    next_row_ = input_.getRow();

    next_comparator_.setSchemaIn1(input_.getSchemaOut());
    next_comparator_.setSchemaIn2(input_.getSchemaOut());
    out_adapter_.setSchemaIn(schema_row);
  }

  @Override
  public void close()
  { input_.close(); }

  @Override
  protected int readRow(String[] result)
  {
    if (buffer_==null) {
      buffer_ = readRowsSameKey();
      buffer_l_ = 0;
      buffer_r_ = 0;
      if (buffer_==null) return -1;
    }

    populateRow(result);
    buffer_r_ ++;
    if (buffer_r_>=buffer_.size()) {
      buffer_l_ ++;
      buffer_r_ = 0;
      if (buffer_l_>=buffer_.size()) {
        buffer_ = null;
      }
    }
    return result.length;
  }

  /**
   * Read and buffer all the rows with the current key for the input
   */
  protected List<String[]> readRowsSameKey()
  {
    if (next_row_==null) {
      return null;
    }

    List<String[]> ret = new ArrayList();
    int sz = next_row_.length;

    String[] row = new String[sz];
    System.arraycopy(next_row_, 0, row, 0, sz);
    ret.add(row);
    next_row_ = input_.getRow();

    while (next_row_!=null && next_comparator_.compare(row, next_row_)==0) {
      row = new String[sz];
      System.arraycopy(next_row_, 0, row, 0, sz);
      ret.add(row);
      next_row_ = input_.getRow();
    }

    return ret;
  }

  /**
   * Populate the results with the next combination of buffered rows
   */
  protected void populateRow(String[] result)
  {
    String[] l_buf = buffer_.get(buffer_l_);
    String[] r_buf = buffer_.get(buffer_r_);
    int j = 0;
    for (int i=0 ; i<l_buf.length ; i++) {
      result[j++] = l_buf[i];
      if ((col_join_ & (1<<i)) == 0) {
        result[j++] = r_buf[i];
      }
    }
  }

  @Override
  protected long getInternalHashCode()
  { return input_.toString().hashCode(); }

  @Override
  public boolean sameAs(Action act)
  { return input_.sameAs(act); }

  @Override
  protected String toStringWhich()
  { return input_.toString(); }
}

