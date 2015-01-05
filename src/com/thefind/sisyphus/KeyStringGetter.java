package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
public class KeyStringGetter
extends Modifier
{
  protected final KeyString kstring_;

  public KeyStringGetter(KeyString kstring)
  { this(kstring, kstring.getSchemaIn().get(0), kstring.getSchemaIn().get(1)); }

  /**
   * Use these constructors only if the query schema doesn't match the KeyDouble schema.
   * This is for the rare case where you have several key columns with different names,
   * and you want to populate corresponding value columns, also with different names.
   */
  public KeyStringGetter(KeyString kstring, String col_key, String col_value)
  {
    super(new String[] { col_key },
          new String[] { col_value });
    kstring_ = kstring;
  }

  public KeyStringGetter(KeyString kstring, String col_value)
  { this(kstring, kstring.getSchemaIn().get(0), col_value); }

  public void compute(String[] input, String[] result)
  {
    try {
      long   key   = Long.parseLong(input[0]);
      String value = kstring_.get(key);
      result[0] = kstring_.get(key);
    } catch (NumberFormatException nfex) {
      result[0] = null;
    }
  }
}

