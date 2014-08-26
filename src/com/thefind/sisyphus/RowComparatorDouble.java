package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class RowComparatorDouble
extends RowComparator
{
  public RowComparatorDouble(String[] schema_sort)
  { super(schema_sort); }

  @Override
  public int compare(final String[] row1, final String[] row2)
  throws NumberFormatException
  {
    if (row1!=null && row2==null) { return 1; }
    if (row1==null && row2==null) { return 0; }
    if (row1==null && row2!=null) { return -1; }

    //note view is a pointer, so copy by value
    String[] view1 = adapter1_.getView(row1);
    String[] view2 = adapter2_.getView(row2);

    for (int i=0; i<view1.length; i++) {
      Double key1 = Double.parseDouble(view1[i]);
      Double key2 = Double.parseDouble(view2[i]);

      int result = key1.compareTo(key2);
      if (result != 0) {
        return result;
      }
    }

    return 0;
  }
}
