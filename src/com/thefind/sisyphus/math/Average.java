package com.thefind.sisyphus.math;

import com.thefind.sisyphus.Modifier;

/**
 * @author Eric Gaudet
 * @author Qin Wang
 */
public class Average
extends Modifier
{
  public Average(String out_col, String... input)
  { super(input, new String[] { out_col }); }

  @Override
  public void compute(String[] input, String[] result)
  {
    double sum = 0.0;
    for (int i = 0; i < input.length; i++) {
      if (input[i] != null) {
        sum += Double.parseDouble(input[i]);
      }
    }
    if (input.length >0) {
      result[0] = String.valueOf(sum/input.length);
    }
    else {
      result[0] = null;
    }
  }
}

