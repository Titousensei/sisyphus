package com.thefind.sisyphus.math;

import com.thefind.sisyphus.Modifier;

/**
 * @author Eric Gaudet
 * @author Qin Wang
 */
public class Divide
extends Modifier
{
  protected final double dividend_;
  protected final double divisor_;

  public Divide(String out_col, String dividend_col, String divisor_col)
  {
    super(new String[] { dividend_col, divisor_col }, new String[] { out_col });
    dividend_ = 0.0;
    divisor_  = 0.0;
  }

  public Divide(String out_col, double dividend, String divisor_col)
  {
    super(new String[] { divisor_col }, new String[] { out_col });
    dividend_ = dividend;
    divisor_  = 0.0;
  }

  public Divide(String out_col, String dividend_col, double divisor)
  {
    super(new String[] { dividend_col }, new String[] { out_col });
    dividend_ = 0.0;
    divisor_  = divisor;
  }

  @Override
  public void compute(String[] input, String[] result)
  {
    if (dividend_!=0.0) {
      if (input[0] != null) {
        double dvsr = Double.parseDouble(input[0]);
        if (dvsr!=0.0) {
          result[0] = String.valueOf(dividend_/dvsr);
          return;
        }
      }
    }
    else if (divisor_!=0.0) {
      if (input[0] != null) {
        double dvd = Double.parseDouble(input[0]);
        result[0] = String.valueOf(dvd/divisor_);
        return;
      }
    }
    else {
      if (input[0] != null && input[1] != null) {
        double dvsr = Double.parseDouble(input[1]);
        if (dvsr!=0.0) {
          result[0] = String.valueOf(Double.parseDouble(input[0])/dvsr);
          return;
        }
      }
    }
    result[0] = null;
  }
}

