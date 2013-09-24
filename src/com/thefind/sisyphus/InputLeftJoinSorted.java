package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

/**
 * @author Eric Gaudet
 */
public class InputLeftJoinSorted
extends InputJoinSorted
{
  protected final int[] comps_;

  public InputLeftJoinSorted(String[] join_schema, Input... inputs)
  {
    super(join_schema, inputs);

    comps_ = new int[inputs.length-1];

    for (int i=0 ; i<nb_inputs_ ; i++) {
      buffers_.add(null);
    }
  }

  @Override
  protected int readRow(String[] result)
  {
    //dumpBuffers();

    if (!has_buffer_) {
      // populate input 0 and reset comparisons
      List<String[]> buf = readRowsSameKey(0);
      if (buf == null) return -1;

      buffers_.set(0, buf);
      buffers_idx_[0] = 0;
      String[] row0 = buffers_.get(0).get(0);
      for (int i=1 ; i<nb_inputs_ ; i++) {
        int j = i-1;
        buf = buffers_.get(i);
        comps_[j] = 1;
        if (buf != null) {
          String[] rowi = buf.get(0);
          comps_[j] = row_comparators_[j].compare(row0, rowi);
        }
      }

      // populate other inputs as necessary
      for (int i=1 ; i<nb_inputs_ ; i++) {
        int j = i-1;
        buffers_idx_[i] = 0;
        while (comps_[j]>0) {
          buf = readRowsSameKey(i);
          buffers_.set(i, buf);
          if (buf == null) {
            comps_[j] = 1;
            break;
          }
          String[] rowi = buf.get(0);
          comps_[j] = row_comparators_[j].compare(row0, rowi);
        }
      }
      has_buffer_ = true;
      //dumpBuffers();
    }

    populateRow(result);
    return result.length;
  }

  @Override
  protected void populateRow(String[] result)
  {
    int last_buf = -1;
    for (int i=nb_inputs_-1 ; i>=0 ; i--) {
      if ((i==0 || comps_[i-1]==0)) {
        List<String[]> buf = buffers_.get(i);
        String[] row = buf.get(buffers_idx_[i]);
        out_adapters_[i].applyView(row, result);
        if (last_buf==-1) {
          last_buf = i;
        }
      }
      else {
        out_adapters_[i].clearView(result);
      }
    }

    //dumpBuffers();
    while (last_buf>=0) {
      buffers_idx_[last_buf] ++;
      if (buffers_.get(last_buf)!=null && buffers_idx_[last_buf] < buffers_.get(last_buf).size()) {
        return;
      }
      else if (last_buf == 0) {
          has_buffer_ = false;
          return;
      }
      else {
        buffers_idx_[last_buf] = 0;
        last_buf --;
      }
    }
  }
}

