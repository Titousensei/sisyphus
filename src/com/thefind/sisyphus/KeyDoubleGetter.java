package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
public class KeyDoubleGetter
extends Modifier
{
  protected final KeyDouble kdouble_;

  public KeyDoubleGetter(KeyDouble kdouble)
  { this(kdouble, kdouble.getSchemaIn().get(0), kdouble.getSchemaIn().get(1)); }

  /**
   * Use these constructors only if the query schema doesn't match the KeyDouble schema.
   * This is for the rare case where you have several key columns with different names,
   * and you want to populate corresponding value columns, also with different names.
   */
  public KeyDoubleGetter(KeyDouble kdouble, String col_key, String col_value)
  {
    super(new String[] { col_key },
          new String[] { col_value });
    kdouble_ = kdouble;
  }

  public KeyDoubleGetter(KeyDouble kdouble, String col_value)
  { this(kdouble, kdouble.getSchemaIn().get(0), col_value); }

  public void compute(String[] input, String[] result)
  {
    try {
      long   key   = Long.parseLong(input[0]);
      double value = kdouble_.get(key);
      if (!Double.isNaN(value)) {
        result[0] = Double.toString(value);
      }
    } catch (NumberFormatException nfex) {
      result[0] = null;
    }
  }
}

