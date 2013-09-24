package com.thefind.sisyphus.math;

import com.thefind.sisyphus.Modifier;

/**
 * @since 0.3.0
 * @author Eric Gaudet
 */
public class StatisticsOneColumn
extends Modifier
{
  protected final String label_;

  protected double sum2_  = 0.0;
  protected double sum1_  = 0.0;
  protected double max_ = Double.NaN;
  protected double min_ = Double.NaN;

  protected int    count_ = 0;

  public StatisticsOneColumn(String label, String input)
  {
    super(new String[] { input }, new String[] {});
    label_ = label;
  }

  @Override
  public void compute(String[] input, String[] result)
  {
    if (input[0] == null || input[0].isEmpty()) {
      return;
    }

    aggregate(Double.parseDouble(input[0]));
  }

  void aggregate(double val)
  {

    if (count_==0) {
      max_ = val;
      min_ = val;
    }
    else if (max_<val) {
      max_ = val;
    }
    else if (min_>val) {
      min_ = val;
    }

    sum2_ += val*val;
    sum1_ += val;
    ++ count_;
  }

  @Override
  public void close()
  {
    System.err.println("[StatisticsOneColumn] Final value --- \""+label_+"\" : "
        +getCount()+" values ["+getMin()+", "+getMax()+"] ~ "+getAverage()+" +/- "+getStdDev());
  }

  public double getAverage()
  { return sum1_ / count_; }

  public double getStdDev()
  { return Math.sqrt( (count_*sum2_ - sum1_*sum1_) / (count_ * (count_-1)) ); }

  public double getSum2()
  { return sum2_; }

  public double getSum()
  { return sum1_; }

  public double getMax()
  { return max_; }

  public double getMin()
  { return min_; }

  public int getCount()
  { return count_; }
}

