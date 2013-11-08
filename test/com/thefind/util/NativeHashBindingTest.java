package com.thefind.util;

import java.util.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import com.thefind.util.NativeHashBinding.Entry;
import com.thefind.util.NativeHashBinding.NativeHashBindingIterator;

/**
 * @author Eric Gaudet
 */
public class NativeHashBindingTest
{
  private NativeHashBinding nhl_;
  private static final long N_KEY = 10000L;
  private static final long T_KEY = N_KEY*3L;
  private static final long H_KEY = N_KEY/30L;

  private int orig_size_;
  private long key_sum_;
  private long val_sum_;

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
    nhl_ = new NativeHashBinding();
    orig_size_ = 0;
    key_sum_ = 0L;
    val_sum_ = 0L;

    for (long j=0L ; j<N_KEY ; j++) {
      if (j%3L==0L) {
        nhl_.put(j, j+1000L);
        key_sum_ += j;
        val_sum_ += j+1000L;
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

    for (long j=1L ; j<T_KEY ; j++) {
      assertFalse("Should not contain "+j, nhl_.contains(j));
    }

  }

  @Test
  public void contains()
  {
    for (long j=0L ; j<T_KEY ; j++) {
      if ((j<N_KEY) && (j%3L==0L)) {
        assertTrue("Should contain "+j, nhl_.contains(j));
      }
      else {
        assertFalse("Should not contain "+j+": "+nhl_.get((long) j), nhl_.contains((long) j));
      }
    }
  }

  @Test
  public void get()
  {
    for (long j=0L ; j<T_KEY ; j++) {
      long c = nhl_.get(j);
      long c_exp = -1L;
      if ((j<N_KEY) && (j%3L==0L)) {
        c_exp = j+1000L;
      }

      assertEquals("Wrong value at "+j, c_exp, c);
    }
  }

  @Test
  public void replace()
  {
    for (long j=0L ; j<H_KEY ; j++) {
      if (j%9L==0L) {
        long ret = nhl_.put((long) j, 13L);
        assertEquals("Wrong old value at "+j, j+1000L, ret);
      }
    }

    for (long j=1 ; j<T_KEY ; j++) {
      long c = nhl_.get((long) j);
      long c_exp = -1L;
      if ((j<N_KEY) && (j%3L==0L)) {
        c_exp = j+1000L;
        if (j<H_KEY && j%9L==0L) {
          c_exp = 13L;
        }
      }

      assertEquals("Wrong value at "+j, c_exp, c);
    }
  }

  @Test
  public void putIfNotPresent()
  {
    int inserted = 0;
    for (long j=100L ; j<H_KEY ; j++) {
      if (j%2L==0L) {
        boolean ret = nhl_.putIfNotPresent(j, j+333L);
        if (j%3L==0L) {
          assertFalse("Wrong return true when present at "+j, ret);
        }
        else {
          assertTrue("Wrong return false when not present at "+j, ret);
          ++ inserted;
        }
      }
    }

    assertEquals("Wrong size", orig_size_ + inserted, nhl_.size());

    for (long j=0L ; j<T_KEY ; j++) {
      long c = nhl_.get(j);
      long c_exp = -1L;
      if ((j<N_KEY) && (j%3L==0L)) {
        c_exp = j+1000L;
      }
      else if ((100L<=j) && (j<H_KEY) && (j%2L==0L)) {
        c_exp = j+333L;
      }

      assertEquals("Wrong value at "+j, c_exp, c);
    }

  }

  @Test
  public void removePresent()
  {
    int num_removed = 0;
    for (long j=100L ; j<H_KEY ; j++) {
      if (j%3L==0L) {
        assertTrue("Wrong return remove present at "+j, nhl_.remove(j));
        ++ num_removed;
      }
    }

    assertEquals("Wrong size", orig_size_ - num_removed, nhl_.size());

    for (long j=0L ; j<T_KEY ; j++) {
      long c = nhl_.get(j);
      long c_exp = -1;
      if ((j<N_KEY) && (j<100L || j>=H_KEY) && (j%3==0)) {
        c_exp = j+1000L;
      }

      assertEquals("Wrong value at "+j, c_exp, c);
    }
  }

  @Test
  public void removeMissing()
  {
    for (long j=100L ; j<H_KEY ; j++) {
      if (j%3L==1L) {
        assertFalse("Wrong return remove present at "+j, nhl_.remove(j));
      }
    }

    assertEquals("Wrong size", orig_size_, nhl_.size());

    for (long j=0 ; j<T_KEY ; j++) {
      long c = nhl_.get(j);
      long c_exp = -1L;
      if ((j<N_KEY) && (j%3L==0L)) {
        c_exp = j+1000L;
      }

      assertEquals("Wrong value at "+j, c_exp, c);
    }
  }

  @Test
  public void iterator()
  {
    long key_sum = 0L;
    long val_sum = 0L;
    Iterator<Entry> it = nhl_.iterator();
    int count = 0;
    while (it.hasNext()) {
      ++ count;
      Entry e = it.next();
      key_sum += e.key;
      val_sum += e.value;
      assertTrue("Key too small: "+e.key, e.key>=0L);
      assertTrue("Key too big: "+e.key, e.key<N_KEY);
      assertSame("Unexpected key: "+e.key, 0L, e.key%3L);
      long c_exp = e.key+1000L;
      assertEquals("Wrong value at "+e.key, c_exp, e.value);
    }

    assertEquals("Wrong count", orig_size_, count);
    assertEquals("Missing keys (wrong checksum)", key_sum_, key_sum);
    assertEquals("Missing values (wrong checksum)", val_sum_, val_sum);
  }

  @Test
  public void iteratorNative()
  {
    long val_sum = 0L;
    NativeHashBindingIterator it = nhl_.nativeIterator();
    int count = 0;
    while (it.hasNext()) {
      ++ count;
      long v = it.nextValue();
      val_sum += v;
    }

    assertEquals("Wrong count", orig_size_, count);
    assertEquals("Missing values (wrong checksum)", val_sum_, val_sum);
  }

  @Test
  public void iteratorSmallTable()
  {
    key_sum_ = 0L;
    val_sum_ = 0L;
    nhl_ = new NativeHashBinding();
    orig_size_ = 0;
    for (long j=0L ; j<10L ; j++) {
      if (j%3L==0L) {
        nhl_.put(j, j+1000L);
        key_sum_ += j;
        val_sum_ += j+1000L;
        ++ orig_size_;
      }
    }

    long key_sum = 0L;
    long val_sum = 0L;
    Iterator<Entry> it = nhl_.iterator();
    int count = 0;
    while (it.hasNext()) {
      ++ count;
      Entry e = it.next();
      key_sum += e.key;
      val_sum += e.value;
      assertTrue("Key too small: "+e.key, e.key>=0L);
      assertTrue("Key too big: "+e.key, e.key<10L);
      assertSame("Unexpected key: "+e.key, 0L, e.key%3L);
      long c_exp = e.key+1000L;
      assertEquals("Wrong value at "+e.key, c_exp, e.value);
    }

    assertEquals("Wrong count", orig_size_, count);
    assertEquals("Missing keys (wrong checksum)", key_sum_, key_sum);
    assertEquals("Missing values (wrong checksum)", val_sum_, val_sum);
  }

  @Test
  public void iteratorRestore()
  {
    long key_sum = 0L;
    long val_sum = 0L;
    NativeHashBindingIterator it1 = nhl_.nativeIterator();
    int count = 0;
    for (int i=0 ; i<100 ; ++ i) {
      ++ count;
      Entry e = it1.next();
      key_sum += e.key;
      val_sum += e.value;
      assertTrue("Key too small: "+e.key, e.key>=0L);
      assertTrue("Key too big: "+e.key, e.key<N_KEY);
      assertSame("Unexpected key: "+e.key, 0L, e.key%3L);
      long c_exp = e.key+1000L;
      assertEquals("Wrong value at "+e.key, c_exp, e.value);
    }

    long saved = it1.getState();

    NativeHashBindingIterator it2 = nhl_.nativeIterator();
    it2.restoreState(saved);
    while (it2.hasNext()) {
      ++ count;
      Entry e = it2.next();
      key_sum += e.key;
      val_sum += e.value;
      assertTrue("Key too small: "+e.key, e.key>=0L);
      assertTrue("Key too big: "+e.key, e.key<N_KEY);
      assertSame("Unexpected key: "+e.key, 0L, e.key%3L);
      long c_exp = e.key+1000L;
      assertEquals("Wrong value at "+e.key, c_exp, e.value);
    }

    assertEquals("Wrong count", orig_size_, count);
    assertEquals("Missing keys (wrong checksum)", key_sum_, key_sum);
    assertEquals("Missing values (wrong checksum)", val_sum_, val_sum);
  }
}

