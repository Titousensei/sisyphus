package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * Container class to create views of an incoming row.
 *
 * - Constructor declares the schema (out) of the view you want to obtain.
 * - setSchemaIn declares the schema (in) of the incoming row.
 *
 * Use getView(row) to receive the view with the desired schema from the incoming row.
 * Use applyView(row) to modify the row with the current view.
 * Use applyExternal(view, row) to modify the row with an external view, using the declared schemas.
 *
 * @author Eric Gaudet
 */
class SchemaAdapter
{
  protected List<String> schema_in_;
  protected final List<String> schema_out_;

  protected final int[] adapter_;
  protected final int size_;

  public SchemaAdapter(String[] schema_in, String[] schema_out)
  { this(CollectionUtil.asConstList(schema_in), CollectionUtil.asConstList(schema_out)); }

  /**
   */
  public SchemaAdapter(List<String> schema_in, List<String> schema_out)
  {
    schema_in_  = schema_in;
    schema_out_ = schema_out;
    adapter_   = new int[schema_out_.size()];

    int sz = 0;
    for (int j = 0 ; j< schema_out_.size() ; j++) {
      String s = schema_out_.get(j);
      adapter_[j] = schema_in_.indexOf(s);
      if (adapter_[j] >= 0) {
        ++ sz;
      }
    }

    size_ = sz;
  }

  public void assertMapAllInput()
  {
    if (size_ != schema_in_.size()) {
      List<String> missing = new ArrayList(schema_in);
      missing.removeAll(schema_out);
      throw new SchemaException("Input "+schema_in_+" doesn't contain columns "+missing+"\" for output "+schema_out_);
    }
  }

  public void assertMapAllOutput()
  {
    if (size_ != schema_in_.size()) {
      List<String> missing = new ArrayList(schema_out);
      missing.removeAll(schema_in);
      throw new SchemaException("Output "+schema_out_+" doesn't contain columns "+missing+"\" from input "+schema_in_);
    }
  }

  public int getSize() { return size_; }

  public List<String> getSchemaIn()
  { return schema_in_; }

  public List<String> getSchemaOut()
  { return schema_out_; }

  /**
   * Receive the view with the desired schema from the incoming row.
   */
  public void apply(String[] input, String[] output)
  {
    for (int j = 0 ; j < output.length ; j++) {
      int i = adapter_[j];
      if (i>=0) {
        output[j] = input[i];
      }
    }
  }

  @Override
  public String toString()
  { return "SchemaAdapter{"+schema_in_+"->"+schema_out_+"}"; }
}

