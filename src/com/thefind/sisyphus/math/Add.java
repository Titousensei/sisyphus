package com.thefind.sisyphus.math;

import com.thefind.sisyphus.Modifier;

/**
 * @author Eric Gaudet
 */
public class Add
extends Modifier
{
  public Add(String out_col, String... in_col)
  { super(in_col, new String[] { out_col }); }

  @Override
  public void compute(String[] input, String[] result)
  {
    double sum = 0.0;
    for (String s : input) {
      if (s!=null) {
        try {
          sum += Double.parseDouble(s);
        }
        catch (NumberFormatException nfex) {
          num_warnings_ ++;
        }
      }
    }
    result[0] = String.valueOf(sum);
  }
}

