package com.thefind.util;

import java.io.*;
import java.util.*;

/**
 * This class stores a set of hashes (native longs) for fast and memory
 * efficient lookup.
 * The add and remove methods are atomic, so read operations are not
 * affected by concurrent writes. Writes are synchronized, but reads are not.
 *
 * @author Eric Gaudet
 */
public class NativeHashSet
extends NativeHash
implements Serializable //, Collection<Long>
{
  private static final long serialVersionUID = -232325788143643684L;

  /**
   * Constructs an empty NativeHashSet with a variable index size.
   * This NativeHashSet uses default parameters for good average performance.
   * <pre>
   * For a 1280M JVM       max   write   3*read  write50%  3*read50%  write1M  3*read1M
   * HashMap:              12M   610u/s  8978u/s   530u/s   8955u/s    553u/s   9346u/s
   * NativeHashSet():     150M  1336u/s  4202u/s  2556u/s   6221u/s   2645u/s  14563u/s
   *</pre>
   * Note: a 1280M JVM can create up to byte[1230000000] = 1173MB
   * actual max is 152M long = 1160MB (8.1 byte/long),
   * hashmap        12M long = 92MB (102.5 byte/long)
   */
  public NativeHashSet()
  { super(); }

  /**
   * Creates an empty NativeHashSet with a constant index size,
   * with optimal performance for up to initialCapacity.
   * Use this contructor to fine-tune performance.
   *
   * The performance will quickly degrade if the size becomes larger
   * than twice the initialCapacity.
   *<pre>
   * For a 1280M JVM       max   write   3*read  write50%  3*read50%  write1M  3*read1M
   * NativeHashSet(100M): 150M  1404u/s  4321u/s  2823u/s   6308u/s   1938u/s  30612u/s
   * NativeHashSet(70M):  150M  1388u/s  4130u/s  2912u/s   6394u/s   1980u/s  30303u/s
   * NativeHashSet(30M):  150M   616u/s  2348u/s  1480u/s   2987u/s   1789u/s  16667u/s
   * NativeHashSet(10M):  150M   419u/s  1531u/s   870u/s   2049u/s   2941u/s  15075u/s
   *</pre>
   * @param  initialCapacity the initial capacity
   * @throws IllegalArgumentException if the initial capacity is negative
   *     or the load factor is nonpositive
   */
  public NativeHashSet(int initialCapacity)
  { super(initialCapacity); }

  protected NativeHashSet(long[][] table, int size, int threshold,
    int loadFactor, boolean contains_zero)
  {
    table_         = table;
    size_          = size;
    threshold_     = threshold;
    loadFactor_    = loadFactor;
    contains_zero_ = contains_zero;
  }

  /**
   * Insert the specified key is in the NativeHashSet.
   * This operation is atomic and does not change the blocks referenced in
   * table_ until the insert is complete.
   * Returns whether it was already there.
   */
  public synchronized boolean add(long key)
  {
    if (key==0) {
      if (!contains_zero_) {
        modCount ++;
        contains_zero_ = true;
        size_++;
        return false;
      }
      else {
        return true;
      }
    }
    int bucket = indexFor((int) key, table_.length);
    long[] block = table_[bucket];
    if (block!=null) {
      int idx = binarySearch(block, key);
      if (idx>=0) {
        return true;
      }
      else {
        idx = -idx-1;
        int len = block.length;
        boolean newlength = false;

        long[] newblock;
        if (block[len-1]==0) {
          //newblock = block;
          newblock = new long[len];
        }
        else {
          newlength = true;
          newblock = new long[len+4];
        }
        System.arraycopy(block, 0, newblock, 0, idx);
        if (idx<len) {
          if (newlength) {
            System.arraycopy(block, idx, newblock, idx+1, len-idx);
          }
          else {
            // if not newlength, the block ends with at least one 0 padding
            // which can be ignored
            System.arraycopy(block, idx, newblock, idx+1, len-idx-1);
          }
        }
        newblock[idx] = key;
        table_[bucket] = newblock;
        if (size_++ >= threshold_) {
          resize(2 * table_.length);
        }
      }
    }
    else {
      block = new long[4];
      table_[bucket] = block;
      block[0] = key;
      // the purpose of resize is to reduce block size for binarySearch,
      // so we don't resize for new block
      size_++;
    }
    modCount ++;
    return false;
  }

  /**
   * Remove the specified key from the NativeHashSet.
   * Does not get the memory back, except if it's the last key of the block.
   * Returns whether it was there.
   */
  @Override
  public boolean remove(long key)
  {
    if (key==0) {
      boolean was = contains_zero_;
      contains_zero_ = false;
      return was;
    }
    int bucket = indexFor((int) key, table_.length);
    long[] block = table_[bucket];
    if (block!=null) {
      int idx = binarySearch(block, key);
      if (idx>=0) {
        int high = block.length - 1;
        while (block[high]==0) {
          high--;
        }
        if (high>=1) {
          System.arraycopy(block, idx+1, block, idx, high-idx);
          block[high]=0;
        }
        else {
          table_[bucket] = null;
        }
        modCount ++;
        size_--;
        return true;
      }
    }
    return false;
  }

  // INTERNAL UTILITIES

  /**
   * Rehashes the contents of this map into a new array with a
   * larger capacity.  This method is called automatically when the
   * number of keys in this map reaches its threshold.
   *
   * If current capacity is MAXIMUM_CAPACITY, this method does not
   * resize the map, but sets threshold to Integer.MAX_VALUE.
   * This has the effect of preventing future calls.
   */
  void resize(int newCapacity)
  {
    long t0=0, t1;
    if (trace_) {
      System.err.println("[NativeHashSet] resizing index to "+newCapacity+", size="+size_);
      t0 = System.currentTimeMillis();
    }
    int oldCapacity = table_.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
      threshold_ = Integer.MAX_VALUE;
      return;
    }
    long[][] newTable = new long[newCapacity][];

    for (int i = 0; i < oldCapacity; i++) {
      long[] e = table_[i];
      if (e != null) {
        int len = e.length;
        long[] e0 = new long[len];
        long[] e1 = new long[len];
        int i0=0;
        int i1=0;
        for (int j=0 ; j<len ; j++) {
          long key = e[j];

          if (key!=0) {
            int bucket = indexFor((int) key, newCapacity);
            if (bucket == i) {
              e0[i0++] = key;
            }
            else {
              e1[i1++] = key;
            }
          }
        }
        if (i0>0) {
          newTable[i] = e0;
        }
        if (i1>0) {
          newTable[i+oldCapacity] = e1;
        }
      }
    }
    table_ = newTable;

    loadFactor_ = size_/MEMORY_PRESSURE;
    if (loadFactor_<4) {
      loadFactor_ = 4;
      threshold_ = newCapacity * loadFactor_;
    }
    else if (loadFactor_<32) {
      threshold_ = newCapacity * loadFactor_;
    }
    else {
      loadFactor_ = 32;
      threshold_ = Integer.MAX_VALUE;
    }

    if (trace_) {
      t1 = System.currentTimeMillis();
      System.err.println("[NativeHashSet] resizing time="+(t1-t0)+" ms, loadFactor="
                         +loadFactor_+" newCapacity="+newCapacity
                         +" threshold="+threshold_);
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb = null;
    for (int i=0 ; i<table_.length ; i++) {
      long[] block = table_[i];
      if (block!=null) {
        for (int j=0 ; j<block.length ; j++) {
          if (sb==null) {
            sb = new StringBuilder("{");
          }
          else {
            sb.append(", ");
          }
          sb.append(block[j]);
        }
      }
    }
    if (sb==null) {
      return "{}";
    }
    sb.append('}');
    return sb.toString();
  }
}
