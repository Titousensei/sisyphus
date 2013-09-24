package com.thefind.sisyphus.math;

import com.thefind.sisyphus.Modifier;

/**
 * @since 0.3.0
 * @author Eric Gaudet
 */
public class StatisticsTwoColumns
extends Modifier
{
  protected final String label_;

  protected double sumX2_  = 0.0;
  protected double sumX_  = 0.0;
  protected double maxX_ = Double.NaN;
  protected double minX_ = Double.NaN;

  protected double sumY2_  = 0.0;
  protected double sumY_  = 0.0;
  protected double maxY_ = Double.NaN;
  protected double minY_ = Double.NaN;

  protected double sumXY_  = 0.0;

  protected int    count_ = 0;

  public StatisticsTwoColumns(String label, String col_x, String col_y)
  {
    super(new String[] { col_x, col_y }, new String[] {});
    label_ = label;
  }

  @Override
  public void compute(String[] input, String[] result)
  {
    if (input[0] == null || input[0].isEmpty()
    ||  input[1] == null || input[1].isEmpty()
    ) {
      return;
    }

    aggregate(Double.parseDouble(input[0]), Double.parseDouble(input[1]));
  }

  void aggregate(double x, double y)
  {
    if (count_==0) {
      maxX_ = x;
      minX_ = x;
      maxY_ = y;
      minY_ = y;
    }
    else {
      if (maxX_<x) {
        maxX_ = x;
      }
      else if (minX_>x) {
        minX_ = x;
      }
      if (maxY_<y) {
        maxY_ = y;
      }
      else if (minY_>y) {
        minY_ = y;
      }
    }

    sumX2_ += x*x;
    sumX_  += x;
    sumY2_ += y*y;
    sumY_  += y;
    sumXY_ += x*y;

    ++ count_;
  }

  @Override
  public void close()
  {
    System.err.println("[StatisticsTwoColumns] Final value --- \""+label_+"\" : "
        +getCount()+" pairs; "
        +"X = ["+getMinX()+", "+getMaxX()+"] ~ "+getAverageX()+" +/- "+getStdDevX()
        +"Y = ["+getMinY()+", "+getMaxY()+"] ~ "+getAverageY()+" +/- "+getStdDevY());
  }


  public double getAverageX()
  { return sumX_ / count_; }

  public double getStdDevX()
  { return Math.sqrt( (count_*sumX2_ - sumX_*sumX_) / (count_ * (count_-1)) ); }

  public double getSumX2()
  { return sumX2_; }

  public double getSumX()
  { return sumX_; }

  public double getMaxX()
  { return maxX_; }

  public double getMinX()
  { return minX_; }


  public double getAverageY()
  { return sumY_ / count_; }

  public double getStdDevY()
  { return Math.sqrt( (count_*sumY2_ - sumY_*sumY_) / (count_ * (count_-1)) ); }

  public double getSumY2()
  { return sumY2_; }

  public double getSumY()
  { return sumY_; }

  public double getMaxY()
  { return maxY_; }

  public double getMinY()
  { return minY_; }


  public double getCovariance()
  { return (sumXY_ - sumX_*sumY_/count_) / count_; }

  public double getCorrelation()
  { return (count_*sumXY_ - sumX_*sumY_) / Math.sqrt((count_*sumX2_ - sumX_*sumX_)*(count_*sumY2_ - sumY_*sumY_)); }
  //{ return getCovariance() / (getStdDevX()*getStdDevY()); }


  public double getOlsSlope()
  { return (count_*sumXY_ - sumX_*sumY_) / (count_*sumX2_ - sumX_*sumX_); }

  public double getOlsOrigin()
  { return (sumY_ - getOlsSlope() * sumX_) / count_; }

  public double getOlsError()
  {
    double slope = getOlsSlope();
    return Math.sqrt((count_*sumY2_ - sumY_*sumY_ - slope*slope*(count_*sumX2_ - sumX_*sumX_)) / (count_ * (count_ -2)));
  }

  public double getOlsErrorSlope()
  {
    double error = getOlsError();
    return Math.sqrt((error*error*count_) / (sumX2_*count_ - sumX_*sumX_));
  }

  public double getOlsErrorOrigin()
  {
    double error = getOlsErrorSlope();
    return Math.sqrt(error*error*sumX2_ / count_);
  }

  public int getCount()
  { return count_; }
}

