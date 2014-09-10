package com.thefind.util;

import java.io.*;
import java.util.*;

/**
 * @author Eric Gaudet
 */
public abstract class NativeHash
implements Iterable, Serializable
{
  private static final long serialVersionUID = -3446210764149242510L;

  /**
   * The capacity - MUST be a power of two.
   */
  protected static final int DEFAULT_INITIAL_CAPACITY = 16;
  protected static final int MAXIMUM_CAPACITY         = 1 << 30;
  protected static final int DEFAULT_LOAD_FACTOR      = 32;

  // MEMORY_PRESSURE=loadFactor/size, with loadFactor>4 and 32=infinite
  protected static final int MEMORY_PRESSURE = 500000;

  /**
   * The table, resized as necessary. Length MUST Always be a power of two.
   */
  protected long[][] table_;

  /**
   * The number of keys contained in this set.
   */
  protected int size_;

  /**
   * The next size value at which to resize (capacity * load factor).
   */
  protected int threshold_;

  /**
   * The load factor for the hash table.
   */
  protected int loadFactor_;

  /**
   * Zero is a special marker for block boundaries,
   * so it need a special treatment to allow storing the key 0
   */
  protected boolean contains_zero_ = false;

  public transient boolean trace_ = false;

  protected transient volatile int modCount = 0;

  public NativeHash()
  {
    loadFactor_ = 4;
    threshold_  = DEFAULT_INITIAL_CAPACITY * loadFactor_;
    table_ = new long[DEFAULT_INITIAL_CAPACITY][];
  }

  protected NativeHash(int initialCapacity)
  {
    if (initialCapacity < 0) {
      throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    }
    else if (initialCapacity > MAXIMUM_CAPACITY) {
      initialCapacity = MAXIMUM_CAPACITY;
    }

    initialCapacity = initialCapacity/DEFAULT_LOAD_FACTOR;
    // Find a power of 2 >= initialCapacity
    int capacity = 1;
    while (capacity < initialCapacity) {
      capacity <<= 1;
    }

    loadFactor_ = Integer.MAX_VALUE;
    threshold_  = Integer.MAX_VALUE;
    table_ = new long[capacity][];
  }

  public NativeHashSet asNativeHashSet()
  { return new NativeHashSet(table_, size_, threshold_, loadFactor_, contains_zero_); }

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map
   */
  public int size()
  { return size_; }

  /**
   * Returns true if this map contains no key-value mappings.
   *
   * @return true if this map contains no key-value mappings
   */
  public boolean isEmpty()
  { return size_ == 0; }

  /**
   * Returns whether the specified key is in the NativeHash.
   */
  public boolean contains(long key)
  {
    if (key==0) {
      return contains_zero_;
    }
    long[] block = table_[indexFor((int) key, table_.length)];
    if (block!=null) {
      return (binarySearch(block, key)>=0);
    }
    return false;
  }

  public abstract boolean remove(long key);

  /**
   * Removes all of the mappings from this map.
   * The map will be empty after this call returns.
   */
  public void clear()
  {
    for (int i = 0; i < table_.length; i++) {
      table_[i] = null;
    }
    size_ = 0;
    contains_zero_ = false;
    modCount = 0;
  }

  // INTERNAL UTILITIES

  /**
   * Returns index for hash code h.
   */
  final static int indexFor(int h, int length)
  { return h & (length-1); }


  /**
   * @return index of the search key
   *         or, -(insertion_point + 1)
   */
  static int binarySearch(long[] block, long key)
  {
    int low = 0;
    int high = block.length - 1;
    while (block[high]==0) {
      high--;
    }

    while (low <= high) {
      int mid = (low + high) >>> 1;
      long midVal = block[mid];

      if (midVal < key) {
        low = mid + 1;
      }
      else if (midVal > key) {
        high = mid - 1;
      }
      else {
       // key found
       return mid;
      }
    }
    // key not found.
    return (-low-1);
  }

  static String toString(long[] block)
  {
    StringBuilder sb = new StringBuilder();
    append(sb, -1, block);
    return sb.toString();
  }

  static void append(StringBuilder sb, int i, long[] block)
  {
    if (block!=null) {
      if (i>=0) {
        sb.append('\n')
          .append(i)
          .append(':');
      }
      for (int j=0 ; j<block.length ; j++) {
        sb.append(' ')
          .append(block[j]);
      }
      sb.append(';');
    }
  }

  public String toStringStats()
  {
    int sum = 0;
    int count = 0;
    int zero = 0;
    int max_e = 0;
    int min_e = Integer.MAX_VALUE;
    for (int i=0 ; i<table_.length ; i++) {
      long[] e = table_[i];
      if (e==null) {
        zero ++;
      }
      else {
        int l = e.length;
        count ++;
        sum += l;
        if (l>max_e) max_e = l;
        if (min_e>l) min_e = l;
      }
    }
    StringBuilder sb = new StringBuilder("NativeHash{");
    if (count>0) {
      sb.append("avg_block_size=")
        .append(sum/count)
        .append(" [")
        .append(min_e)
        .append("..")
        .append(max_e)
        .append("] size=")
        .append(size_);
    }
    if (loadFactor_<Integer.MAX_VALUE) {
      sb.append(" loadFactor=")
        .append(loadFactor_);
    }
    sb.append(" index_size=")
      .append(table_.length);
    if (contains_zero_) {
      sb.append(" zero");
    }
    if (zero!=0) {
      sb.append(" zero=")
        .append(zero);
    }
    sb.append(" count=")
      .append(count)
      .append("}");
    return sb.toString();
  }

  public Iterator<Long> iterator()
  { return new NativeHashIterator(); }

  public NativeHashIterator keyIterator()
  { return new NativeHashIterator(); }

  public final class NativeHashIterator
  implements Iterator<Long>
  {
    private int bucket;
    private int idx;
    private int expectedModCount;     // For fast-fail
    private long[] entry;

    private NativeHashIterator()
    {
      expectedModCount = modCount;
      if (size_ > 0) {
        entry = null;
        bucket = -1;
        if (!contains_zero_) {
          advanceEntry();
        }
      }
    }

    private void advanceEntry()
    {
      while (true) {
        idx ++;
        if (entry!=null && idx<entry.length) {
          if (entry[idx]!=0) {
            break;
          }
        }
        else {
          idx = 0;
          entry = null;
          while (entry==null && (++bucket)<table_.length) {
            entry = table_[bucket];
          }
          break;
        }
      }
    }

    public long nextKey()
    {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if (bucket==-1 && contains_zero_) {
        advanceEntry();
        return 0L;
      }
      if (entry == null) {
        throw new NoSuchElementException();
      }

      long ret = entry[idx];
      advanceEntry();
      return ret;
    }

    @Override
    public boolean hasNext()
    { return ((entry != null) || (bucket < 0)); }

    @Override
    public Long next()
    { return Long.valueOf(nextKey()); }

    @Override
    public void remove()
    { throw new UnsupportedOperationException(); }
  }
}
