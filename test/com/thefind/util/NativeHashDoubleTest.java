package com.thefind.util;

import java.util.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Gaudet
 */
public class NativeHashDoubleTest
{
  private NativeHashDouble nhl_;
  private static final int N_KEY = 10000;
  private static final int T_KEY = N_KEY*3;
  private static final int H_KEY = N_KEY/30;

  private int orig_size_;

  @BeforeClass
  public static void runBeforeOnce() {
    //run one time before all tests
  }

  @AfterClass
  public static void runAfterOnce() {
    //run one time after all tests
  }

  @Before
  public void runBeforeEveryTest()
  {
    // create and populate the test NativeHashLookup
    nhl_ = new NativeHashDouble();
    orig_size_ = 0;

    for (int j=0 ; j<N_KEY ; j++) {
      if (j%3==0) {
        nhl_.put((long) j,j+1000.1);
        ++ orig_size_;
      }
    }
  }

  @After
  public void runAfterEveryTest()
  { nhl_ = null; }

  @Test
  public void put()
  {
    assertEquals("Wrong size "+nhl_.size(), orig_size_, nhl_.size());
  }

  @Test
  public void clear()
  {
    nhl_.clear();

    assertEquals(nhl_.size(), 0);

    for (int j=1 ; j<T_KEY ; j++) {
      assertFalse("Should not contain "+j, nhl_.contains((long) j));
    }

  }

  @Test
  public void contains()
  {
    for (int j=0 ; j<T_KEY ; j++) {
      if ((j<N_KEY) && (j%3==0)) {
        assertTrue("Should contain "+j, nhl_.contains((long) j));
      }
      else {
        assertFalse("Should not contain "+j+": "+nhl_.get((long) j), nhl_.contains((long) j));
      }
    }
  }

  @Test
  public void get()
  {
    for (int j=0 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void replace()
  {
    for (int j=0 ; j<H_KEY ; j++) {
      if (j%9==0) {
        double ret = nhl_.put((long) j, 13.9);
        assertEquals("Wrong old value at "+j, j+1000.1, ret, 0.0001);
      }
    }

    for (int j=1 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        if (j<H_KEY && j%9==0) {
          c_exp = 13.9;
        }
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void incrementExisting()
  {
    for (int j=0 ; j<H_KEY ; j++) {
      if (j%9==0) {
        double ret = nhl_.increment((long) j, 7.2);
        assertEquals("Wrong new value at "+j, j+1000.1+7.2, ret, 0.0001);
      }
    }

    for (int j=1 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        if (j<H_KEY && j%9==0) {
          c_exp += 7.2;
        }
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void incrementMissing()
  {
    for (int j=0 ; j<H_KEY ; j++) {
      if (j%9==1) {
        double ret = nhl_.increment((long) j, 11.3);
        assertEquals("Wrong new value at "+j, 0.0+11.3, ret, 0.0001);
      }
    }

    for (int j=0 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else if ((j<H_KEY) && (j%9==1)) {
        double c_exp = 11.3;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void multiplyExisting()
  {
    for (int j=0 ; j<H_KEY ; j++) {
      if (j%9==0) {
        double ret = nhl_.multiply((long) j, 7.2);
        assertEquals("Wrong new value at "+j, (j+1000.1)*7.2, ret, 0.0001);
      }
    }

    for (int j=1 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        if (j<H_KEY && j%9==0) {
          c_exp *= 7.2;
        }
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void multiplyMissing()
  {
    for (int j=0 ; j<H_KEY ; j++) {
      if (j%9==1) {
        double ret = nhl_.multiply((long) j, 11.3);
        assertTrue("Wrong new value at "+j, Double.isNaN(ret));
      }
    }

    for (int j=0 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void putIfNotPresent()
  {
    int inserted = 0;
    for (int j=100 ; j<H_KEY ; j++) {
      if (j%2==0) {
        boolean ret = nhl_.putIfNotPresent((long) j, j+333.333);
        if (j%3==0) {
          assertFalse("Wrong return true when present at "+j, ret);
        }
        else {
          assertTrue("Wrong return false when not present at "+j, ret);
          ++ inserted;
        }
      }
    }

    assertEquals("Wrong size", orig_size_ + inserted, nhl_.size());

    for (int j=0 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else if ((100<=j) && (j<H_KEY) && (j%2==0)) {
        double c_exp = j+333.333;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void removePresent()
  {
    int num_removed = 0;
    for (int j=100 ; j<H_KEY ; j++) {
      if (j%3==0) {
        assertTrue("Wrong return remove present at "+j, nhl_.remove(j));
        ++ num_removed;
      }
    }

    assertEquals("Wrong size", orig_size_ - num_removed, nhl_.size());

    for (int j=0 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j<100 || j>=H_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }
    }
  }

  @Test
  public void removeMissing()
  {
    for (int j=100 ; j<H_KEY ; j++) {
      if (j%3==1) {
        assertFalse("Wrong return remove present at "+j, nhl_.remove(j));
      }
    }

    assertEquals("Wrong size", orig_size_, nhl_.size());

    for (int j=0 ; j<T_KEY ; j++) {
      double c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        double c_exp = j+1000.1;
        assertEquals("Wrong value at "+j, c_exp, c, 0.0001);
      }
      else {
        assertTrue("Wrong value at "+j, Double.isNaN(c));
      }

    }
  }
}

