package com.thefind.sisyphus.math;

import java.util.*;

import org.junit.Test;

import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 * @author Eric Gaudet
 */
public class StatisticsTwoColumnsTest
{
  private static StatisticsTwoColumns _stats;

  private static final double DELTA = 0.0000000001;

  // Example from wikipedia Simple_linear_regression
  private static final double[] X = new double[] {
    1.47, 1.50, 1.52, 1.55, 1.57, 1.60, 1.63, 1.65, 1.68, 1.70, 1.73, 1.75, 1.78, 1.80, 1.83
  };

  private static final double[] Y = new double[] {
    52.21, 53.12, 54.48, 55.84, 57.20, 58.57, 59.93, 61.29, 63.11, 64.47, 66.28, 68.10, 69.92, 72.19, 74.46
  };

  private static int _count = 0;

  @BeforeClass
  public static void runBeforeOnce()
  {
    _stats = new StatisticsTwoColumns(null, null, null);
    _count = X.length;
    for (int i=0 ; i<_count ; i++) {
      _stats.aggregate(X[i], Y[i]);
    }
  }


  @Test
  public void getAverageX()
  { assertEquals(1.6506666667, _stats.getAverageX(), DELTA); }

  @Test
  public void getStdDevX()
  { assertEquals(0.1142345123, _stats.getStdDevX(), DELTA); }

  @Test
  public void getSumX2()
  { assertEquals(41.0532, _stats.getSumX2(), DELTA); }

  @Test
  public void getSumX()
  { assertEquals(24.76, _stats.getSumX(), DELTA); }

  @Test
  public void getMaxX()
  { assertEquals(1.83, _stats.getMaxX(), DELTA); }

  @Test
  public void getMinX()
  { assertEquals(1.47, _stats.getMinX(), DELTA); }


  @Test
  public void getAverageY()
  { assertEquals(62.078, _stats.getAverageY(), DELTA); }

  @Test
  public void getStdDevY()
  { assertEquals(7.0375149835, _stats.getStdDevY(), DELTA); }

  @Test
  public void getSumY2()
  { assertEquals(58498.5439, _stats.getSumY2(), DELTA); }

  @Test
  public void getSumY()
  { assertEquals(931.17, _stats.getSumY(), DELTA); }

  @Test
  public void getMaxY()
  { assertEquals(74.46, _stats.getMaxY(), DELTA); }

  @Test
  public void getMinY()
  { assertEquals(52.21, _stats.getMinY(), DELTA); }


  @Test
  public void getCovariance()
  { assertEquals(0.746268, _stats.getCovariance(), DELTA); }

  @Test
  public void getCorrelation()
  { assertEquals(0.9945837935768758, _stats.getCorrelation(), DELTA); }


  @Test
  public void getOlsSlope()
  { assertEquals(61.272186542107434, _stats.getOlsSlope(), DELTA); }

  @Test
  public void getOlsOrigin()
  { assertEquals(-39.061955918838656, _stats.getOlsOrigin(), DELTA); }

  @Test
  public void getOlsError2()
  { assertEquals(0.7590762809494941, _stats.getOlsError(), DELTA); }

  @Test
  public void getOlsErrorSlope2()
  { assertEquals(1.7759227522175622, _stats.getOlsErrorSlope(), DELTA); }

  @Test
  public void getOlsErrorOrigin2()
  { assertEquals(2.938001067187078, _stats.getOlsErrorOrigin(), DELTA); }


  @Test
  public void getCount()
  { assertEquals(_count, _stats.getCount(), DELTA); }
}

