package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
public class KeyDoubleGetter
extends Modifier
{
  protected final KeyDouble kdouble_;

  public KeyDoubleGetter(KeyDouble kdouble)
  {
    super(new String[] { kdouble.getSchemaIn().get(0) },
          new String[] { kdouble.getSchemaIn().get(1) });
    kdouble_ = kdouble;
  }

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

