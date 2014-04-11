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
public class NativeHashObjectTest
{
  private NativeHashObject<String> nhl_;
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
    // create and populate the test NativeHashObject
    nhl_ = new NativeHashObject();
    orig_size_ = 0;

    for (int j=0 ; j<N_KEY ; j++) {
      if (j%3==0) {
        nhl_.put((long) j, String.valueOf(j+1000.1));
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
      String c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        String c_exp = String.valueOf(j+1000.1);
        assertEquals("Wrong value at "+j, c_exp, c);
      }
      else {
        assertTrue("Wrong value at "+j, (c==null));
      }
    }
  }

  @Test
  public void replace()
  {
    for (int j=0 ; j<H_KEY ; j++) {
      if (j%9==0) {
        String ret = nhl_.put((long) j, "13.9");
        assertEquals("Wrong old value at "+j, String.valueOf(j+1000.1), ret);
      }
    }

    for (int j=1 ; j<T_KEY ; j++) {
      String c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        String c_exp = String.valueOf(j+1000.1);
        if (j<H_KEY && j%9==0) {
          c_exp = "13.9";
        }
        assertEquals("Wrong value at "+j, c_exp, c);
      }
      else {
        assertTrue("Wrong value at "+j, (c==null));
      }
    }
  }

  @Test
  public void putIfNotPresent()
  {
    int inserted = 0;
    for (int j=100 ; j<H_KEY ; j++) {
      if (j%2==0) {
        boolean ret = nhl_.putIfNotPresent((long) j, String.valueOf(j+333.333));
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
      String c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        String c_exp = String.valueOf(j+1000.1);
        assertEquals("Wrong value at "+j, c_exp, c);
      }
      else if ((100<=j) && (j<H_KEY) && (j%2==0)) {
        String c_exp = String.valueOf(j+333.333);
        assertEquals("Wrong value at "+j, c_exp, c);
      }
      else {
        assertTrue("Wrong value at "+j, (c==null));
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
      String c = nhl_.get((long) j);
      if ((j<N_KEY) && (j<100 || j>=H_KEY) && (j%3==0)) {
        String c_exp = String.valueOf(j+1000.1);
        assertEquals("Wrong value at "+j, c_exp, c);
      }
      else {
        assertTrue("Wrong value at "+j, (c==null));
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
      String c = nhl_.get((long) j);
      if ((j<N_KEY) && (j%3==0)) {
        String c_exp = String.valueOf(j+1000.1);
        assertEquals("Wrong value at "+j, c_exp, c);
      }
      else {
        assertTrue("Wrong value at "+j, (c==null));
      }

    }
  }
}

