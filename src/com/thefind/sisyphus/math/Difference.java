package com.thefind.sisyphus.math;

import com.thefind.sisyphus.Modifier;

/**
 * @author Eric Gaudet
 */
public class Difference
extends Modifier
{
  public static enum Mode { NORMAL, ABS };

  private final Mode mode_;

  public Difference(String out_col, String... in_col)
  { this(out_col, Mode.NORMAL, in_col); }

  public Difference(String out_col, Mode mode, String... in_col)
  {
    super(in_col, new String[] { out_col });
    mode_ = mode;
  }

  @Override
  public void compute(String[] input, String[] result)
  {
    if (input[0]!=null && input[1]!=null) {
      try {
        double diff = Double.parseDouble(input[0]) - Double.parseDouble(input[1]);
        if (mode_==Mode.ABS) {
          diff = Math.abs(diff);
        }
        result[0] = String.valueOf(diff);
      }
      catch (NumberFormatException nfex) {
        num_warnings_ ++;
      }
    }
  }
}

