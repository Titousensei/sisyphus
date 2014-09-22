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

  protected final String[] container_;
  protected final int[] adapter_;
  protected final int size_;

  public SchemaAdapter(String[] schema_out)
  { this(CollectionUtil.asConstList(schema_out)); }

  /**
   * Declare the schema of the view you want to obtain.
   */
  public SchemaAdapter(List<String> schema_out)
  {
    schema_out_ = schema_out;

    size_      = schema_out_.size();
    container_ = new String[size_];
    adapter_   = new int[size_];
  }

  public int getSize() { return size_; }

  public List<String> getSchemaOut()
  { return schema_out_; }

  /**
   * Declare the schema of the incoming row.
   */
  public void setSchemaIn(List<String> schema_in)
  {
    schema_in_ = schema_in;

    for (int i = 0 ; i < size_ ; i++) {
      adapter_[i] = -1;
    }

    int k = 0;
    for (String s : schema_out_) {
      int j = schema_in_.indexOf(s);
      if (j>=0) {
        adapter_[k++] = j;
      }
      else {
        throw new SchemaException("Input "+schema_in_+" doesn't contain column \""+s+"\" for output "+schema_out_);
      }
    }
  }

  /**
   * Receive the view with the desired schema from the incoming row.
   */
  public String[] getView(String[] input)
  {
    for (int i = 0 ; i < size_ ; i++) {
      container_[i] = input[adapter_[i]];
    }
    return container_;
  }

  /**
   * Receive the view with the desired schema from the incoming row.
   */
  public void getView(String[] external, String[] input)
  {
    for (int i = 0 ; i < size_ ; i++) {
      external[i] = input[adapter_[i]];
    }
  }

  /**
   * Modify the row by copying the current view to it.
   */
  public void applyView(String[] result)
  {
    for (int i = 0 ; i < size_ ; i++) {
      result[adapter_[i]] = container_[i];
    }
  }

  /**
   * Modify the row by copying the current view to it.
   */
  public void clearView(String[] result)
  {
    for (int i = 0 ; i < size_ ; i++) {
      result[adapter_[i]] = null;
    }
  }

  /**
   * Modify the row with an external view, using the declared schemas.
   */
  public void applyView(String[] container, String[] result)
  {
    for (int i = 0 ; i < size_ ; i++) {
      result[adapter_[i]] = container[i];
    }
  }

  /**
   * Get the current view.
   */
  public String[] getContainer()
  { return container_; }

  /**
   * Clears the current view.
   */
  public void clearContainer()
  { Arrays.fill(container_, null); }

  /**
   * Get the current view.
   */
  public String[] newContainer()
  { return new String[size_]; }

  @Override
  public String toString()
  { return "SchemaAdapter{"+schema_in_+"->"+schema_out_+"}"; }
}

