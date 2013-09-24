package com.thefind.sisyphus.math;

import java.util.*;

import org.junit.Test;

import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 * @author Eric Gaudet
 */
public class StatisticsOneColumnTest
{
  private static StatisticsOneColumn _stats;

  private static final double DELTA = 0.0000000001;

  private static final double[] Y = new double[] {
    52.21, 53.12, 54.48, 55.84, 57.20, 58.57, 59.93, 61.29, 63.11, 64.47, 66.28, 68.10, 69.92, 72.19, 74.46
  };

  private static int _count = 0;

  @BeforeClass
  public static void runBeforeOnce()
  {
    _stats = new StatisticsOneColumn(null, null);
    _count = Y.length;
    for (int i=0 ; i<_count ; i++) {
      _stats.aggregate(Y[i]);
    }
  }


  @Test
  public void getAverage()
  { assertEquals(62.078, _stats.getAverage(), DELTA); }

  @Test
  public void getStdDev()
  { assertEquals(7.0375149835, _stats.getStdDev(), DELTA); }

  @Test
  public void getSum2()
  { assertEquals(58498.5439, _stats.getSum2(), DELTA); }

  @Test
  public void getSum()
  { assertEquals(931.17, _stats.getSum(), DELTA); }

  @Test
  public void getMax()
  { assertEquals(74.46, _stats.getMax(), DELTA); }

  @Test
  public void getMin()
  { assertEquals(52.21, _stats.getMin(), DELTA); }


  @Test
  public void getCount()
  { assertEquals(_count, _stats.getCount(), DELTA); }
}

