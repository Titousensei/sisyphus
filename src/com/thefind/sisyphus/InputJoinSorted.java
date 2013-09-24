package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

/**
 * @author Eric Gaudet
 */
public class InputJoinSorted
extends Input
{
  protected final Input[] inputs_;

  protected final SchemaAdapter[] out_adapters_;
  protected final RowComparator[] row_comparators_;
  protected final RowComparator[] next_comparators_;
  protected final int nb_inputs_;

  protected final String str_;

  protected String[][] rows_;

  protected List<List<String[]>> buffers_;
  protected int[] buffers_idx_;
  protected boolean has_buffer_;

  /**
   * Columns will be written in reverse order of the inputs,
   * hence the first input is the more important and will
   * overwrite the columns of previous inputs.
   */
  public InputJoinSorted(String[] join_schema, Input... inputs)
  {
    super(Input.mergedSchema(inputs));

    inputs_ = inputs;

    nb_inputs_   = inputs_.length;
    rows_        = new String[nb_inputs_][];
    buffers_     = new ArrayList(nb_inputs_);
    buffers_idx_ = new int[nb_inputs_];
    has_buffer_  = false;

    out_adapters_ = new SchemaAdapter[nb_inputs_];

    row_comparators_  = new RowComparator[nb_inputs_-1];
    next_comparators_ = new RowComparator[nb_inputs_];

    for (int i=0 ; i<nb_inputs_ ; i++) {
      out_adapters_[i] = new SchemaAdapter(inputs_[i].getSchemaOut());

      if (i>0) {
        row_comparators_[i-1] = new RowComparator(join_schema);
      }
      next_comparators_[i] = new RowComparator(join_schema);
    }

    StringBuilder sb = null;
    for (Input in : inputs_) {
      if (sb==null) {
        sb = new StringBuilder();
      }
      else {
        sb.append(", ");
      }
      sb.append(in.toString());
    }
    str_ = sb.toString();
  }

  @Override
  public boolean open()
  {
    for (int i=0 ; i<nb_inputs_ ; i++) {
      if (!inputs_[i].open()) {
        System.err.println("[InputJoinSorted] ERROR - Could not open "+inputs_[i]);
        return false;
      }
      System.err.println("[InputJoinSorted] open: "+inputs_[i]);
    }

    return true;
  }

  @Override
  public void ready(List<String> schema_row)
  {
    super.ready(schema_row);

    for (int i=0 ; i<nb_inputs_ ; i++) {
      inputs_[i].ready(inputs_[i].getSchemaOut());
      rows_[i] = inputs_[i].getRow();

      if (i>0) {
        row_comparators_[i-1].setSchemaIn1(inputs_[0].getSchemaOut());
        row_comparators_[i-1].setSchemaIn2(inputs_[i].getSchemaOut());
      }
      next_comparators_[i].setSchemaIn1(inputs_[i].getSchemaOut());
      next_comparators_[i].setSchemaIn2(inputs_[i].getSchemaOut());
      out_adapters_[i].setSchemaIn(schema_row);
    }
  }

  @Override
  public void close()
  {
    for (Input input : inputs_) {
      input.close();
      System.err.println("[InputJoinSorted] close: "+input);
    }
  }

  @Override
  protected int readRow(String[] result)
  {
    if (has_buffer_) {
      populateRow(result);
      return result.length;
    }

    while (true) {
      int found = findMatch();
      switch (found) {
      case 0:
        if (rows_[0]==null) {
          return -1;
        }
        break;
      case 1:
        // populate
        for (int i=0 ; i<nb_inputs_ ; i++) {
          buffers_.add(readRowsSameKey(i));
        }
        has_buffer_ = true;
        populateRow(result);
        return result.length;
      default:
        return -1;
      }
    }
  }

  /**
   * @returns 1 for found, 0 for miss, -1 for EOF
   */
  protected int findMatch()
  {
    if (rows_[0]==null) { return -1; }

    for (int j=1 ; j<nb_inputs_ ; j++) {
      if (rows_[j]==null) { return -1; }
      int i = j-1;
      int c1 = row_comparators_[i].compare(rows_[0], rows_[j]);

      if (c1<0) {
        for (int k=0 ; k<j ; k++) {
          rows_[k] = inputs_[k].getRow();
          if (rows_[k]==null) { return -1; }
        }
        return 0;
      }

      while (c1>0) {
        rows_[j] = inputs_[j].getRow();
        if (rows_[j]==null) { return -1; }
        c1 = row_comparators_[i].compare(rows_[0], rows_[j]);
        if (c1<0) {
          for (int k=0 ; k<j ; k++) {
            rows_[k] = inputs_[k].getRow();
            if (rows_[k]==null) { return -1; }
          }
          return 0;
        }
      }
    }

    return 1;
  }

  /**
   * Read and buffer all the rows with the current key for a given input
   */
  protected List<String[]> readRowsSameKey(int i)
  {
    if (rows_[i]==null) { return null; }

    int sz = rows_[i].length;
    String[] buffer;

    buffer = new String[sz];
    System.arraycopy(rows_[i], 0, buffer, 0, sz);
    List<String[]> ret = new ArrayList();
    ret.add(buffer);
    rows_[i] = inputs_[i].getRow();

    while (rows_[i]!=null && next_comparators_[i].compare(buffer, rows_[i])==0) {
      buffer = new String[sz];
      System.arraycopy(rows_[i], 0, buffer, 0, sz);
      ret.add(buffer);
      rows_[i] = inputs_[i].getRow();
    }

    return ret;
  }

  /**
   * Populate the results with the next combination of buffered rows
   */
  protected void populateRow(String[] result)
  {
    for (int i=nb_inputs_-1 ; i>=0 ; i--) {
      List<String[]> buf = buffers_.get(i);
      String[] row = buf.get(buffers_idx_[i]);
      out_adapters_[i].applyView(row, result);
    }

    for (int i=nb_inputs_-1 ; i>=0 ; i--) {
      buffers_idx_[i]++;
      List<String[]> buf = buffers_.get(i);
      if (buffers_idx_[i]<buf.size()) {
        return;
      }
      else {
        buffers_idx_[i] = 0;
      }
    }

    buffers_.clear();
    has_buffer_ = false;
  }

  @Override
  protected long getInternalHashCode()
  { return str_.hashCode(); }

  @Override
  public boolean sameAs(Action act)
  {
    for (Input in : inputs_) {
      if (in.sameAs(act)) { return true; }
    }
    return false;
  }

  @Override
  protected String toStringWhich()
  { return str_; }

  protected void dumpBuffers()
  {
    System.out.println("[InputJoinSorted] DEBUG === has_buffer_ "+has_buffer_);
    int i = 0;
    for (List<String[]> buf : buffers_) {
      System.out.println("[InputJoinSorted] DEBUG - buffers_idx_ "+i+" "+buffers_idx_[i]);
      if (buf==null) {
          System.out.println("[InputJoinSorted] DEBUG - null");
      }
      else {
        int j = 0;
        for (String[] row : buf) {
          System.out.println("[InputJoinSorted] DEBUG - "+j
            +( (j==buffers_idx_[i]) ? ">": " ")
             +Arrays.toString(row));
          j ++;
        }
      }
      i ++;
    }
  }
}

