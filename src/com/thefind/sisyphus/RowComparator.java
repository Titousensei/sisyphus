package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
class RowComparator
implements Comparator<String[]>
{
  protected final SchemaAdapter adapter1_;
  protected final SchemaAdapter adapter2_;

  public boolean debug_ = false;

  public RowComparator(String[] schema_sort)
  {
    adapter1_ = new SchemaAdapter(schema_sort);
    adapter2_ = new SchemaAdapter(schema_sort);
  }

  public RowComparator(List<String> schema_sort)
  {
    adapter1_ = new SchemaAdapter(schema_sort);
    adapter2_ = new SchemaAdapter(schema_sort);
  }

  public List<String> getSchemaSort()
  { return adapter1_.getSchemaOut(); }

  protected void setSchemaIn1(List<String> schema_row)
  { adapter1_.setSchemaIn(schema_row); }

  protected void setSchemaIn2(List<String> schema_row)
  { adapter2_.setSchemaIn(schema_row); }

  public String[] getView(String[] row)
  { return adapter1_.getView(row); }

  @Override
  public int compare(final String[] row1, final String[] row2)
  {
    if (row1!=null && row2==null) { return 1; }
    if (row1==null && row2==null) { return 0; }
    if (row1==null && row2!=null) { return -1; }

    //note view is a pointer, so copy by value
    String[] view1 = adapter1_.getView(row1);
    String[] view2 = adapter2_.getView(row2);

    for (int i=0; i<view1.length ; i++) {
      int result = view1[i].compareTo(view2[i]);

      if (result != 0) {
        if (debug_) System.err.println("[RowComparator] DEBUG "+result+" "+Arrays.asList(view1)+" "+Arrays.asList(view2));
        return result;
      }
    }

    if (debug_) System.err.println("[RowComparator] DEBUG 0 "+Arrays.asList(view1)+" "+Arrays.asList(view2));
    return 0;
  }

  public String toString()
  { return getClass().getSimpleName()+"{"+adapter1_.getSchemaOut()+"}"; }
}

