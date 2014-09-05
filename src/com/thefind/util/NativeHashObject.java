package com.thefind.util;

import java.io.Serializable;
import java.util.Iterator;

/**
 * This class stores a set of hashes (native longs) for fast and memory
 * efficient lookup.
 * This class doesn't have an atomic put(), so get() can return the wrong result
 * if another thread is writing at the same time. Use getSynchronized instead.
 *
 * Can be used in combination with an array or an ArrayList
 * as memory-efficient hashmaps of objects.
 * <pre>
 *   NativeHashObject lookup = new NativeHashObject();
 *   List<String> data = new ArrayList();
 *   // or String[] data = new String[len];
 *
 *   // Writing
 *   String row = file.readLine();
 *   int idx = 0;
 *   while (row!=null) {
 *     String[] keyvalue = row.split("=");
 *     long hash = WhUtil.hashUrl(keyvalue[0]);
 *     if (lookup.putIfNotPresent(hash, idx)) {
 *        // optional: also store keyvalue[0] if iterating through keys is needed
 *        data[idx] = keyvalue[1];
 *        idx ++;
 *     }
 *     row = file.readLine();
 *   }
 *
 *   // Reading
 *   String get(String key)
 *   {
 *     long hash = WhUtil.hashUrl(key);
 *     int i = lookup.get(hash);
 *     if (i>=0)
 *       return data[i];
 *     else
 *       return null;
 *   }
 * </pre>
 *   For very large string arrays, append the strings to a file
 *   and lookup the seek positions.
 *
 * @author Eric Gaudet
 * @author Madhur Khandelwal
 */
public class NativeHashObject<T>
extends NativeHash
implements Serializable //, Collection<T>
{
//  private static final long serialVersionUID = -5175757327674218923L;

  protected T[][] value_;
  protected T value_zero_;

  /**
   * Constructs an empty NativeHashObject with a variable index size.
   * This NativeHashObject uses default parameters for good average performance.
   * The order of the keys is not guaranteed to be stable and can change when
   * the index is resized.
   */
  public NativeHashObject()
  {
    super();
    //@SuppressWarnings("unchecked")
    value_ = (T[][]) new Object[DEFAULT_INITIAL_CAPACITY][];
    value_zero_ = null;
  }

  /**
   * Creates an empty NativeHashObject with a constant index size,
   * with optimal performance for up to initialCapacity.
   * Since the index is never resized, the order of the keys doesn't change.
   * Use this contructor to fine-tune performance or if you need a stable
   * order for the keys.
   *
   * The performance will quickly degrade if the size becomes larger
   * than twice the initialCapacity.
   *
   * @param  initialCapacity the initial capacity
   * @throws IllegalArgumentException if the initial capacity is negative
   *     or the load factor is nonpositive
   */
  public NativeHashObject(int initialCapacity)
  {
    super(initialCapacity);
    value_ = (T[][]) new Object[table_.length][];
    value_zero_ = null;
  }

  /**
   * Returns the value associated with the specified key
   */
  public T get(long key)
  {
    if (key==0) {
      if (contains_zero_) {
        return value_zero_;
      }
      else {
        return null;
      }
    }

    int bucket = indexFor((int) key, table_.length);
    long[] block = table_[bucket];
    if (block!=null) {
      int pos = binarySearch(block, key);
      if (pos>=0) {
        return value_[bucket][pos];
      }
    }

    return null;
  }

  public synchronized T getSynchronized(long key)
  { return get(key); }

  /**
   * Tests if the value is associated with the specified key
   * @return 1 if match, 0 if different, -1 if not found
   */
  public int contains(long key, T value)
  {
    if (key==0) {
      if (contains_zero_) {
        return (value_zero_==value) ? 1 : 0;
      }
      else {
        return -1;
      }
    }

    int bucket = indexFor((int) key, table_.length);
    long[] block = table_[bucket];
    if (block!=null) {
      int pos = binarySearch(block, key);
      if (pos>=0) {
        return (value_[bucket][pos]==value) ? 1 : 0;
      }
    }
    return -1;
  }

  /**
   * Insert the specified key is in the NativeHashObject.
   * Returns the value it had.
   */
  public synchronized T put(long key, T value)
  {
    if (key==0) {
      if (!contains_zero_) {
        contains_zero_ = true;
        size_++;
      }
      T was = value_zero_;
      value_zero_ = value;
      return was;
    }

    int bucket = indexFor((int) key, table_.length);
    long[] block = table_[bucket];
    if (block!=null) {
      int idx = binarySearch(block, key);
      if (idx>=0) {
        T ret = value_[bucket][idx];
        value_[bucket][idx] = value;
        return ret;
      }
      else {
        insertNewKey(block, bucket, -idx-1, key, value);
      }
    }
    else {
      insertNewBlock(bucket, key, value);
    }

    return null;
  }

  /**
   * Insert the specified key is in the NativeHashObject
   * ONLY if it's not already present.
   * Returns whether the value was new and inserted or not.
   */
  public synchronized boolean putIfNotPresent(long key, T value)
  {
    if (key==0) {
      if (contains_zero_) {
        return false;
      }
      else {
        contains_zero_ = true;
        value_zero_ = value;
        return true;
      }
    }
    int bucket = indexFor((int) key, table_.length);
    long[] block = table_[bucket];
    if (block!=null) {
      int idx = binarySearch(block, key);
      if (idx>=0) {
        return false;
      }
      else {
        insertNewKey(block, bucket, -idx-1, key, value);
      }
    }
    else {
      insertNewBlock(bucket, key, value);
    }
    return true;
  }

  /**
   * Insert a new key in an existing block,
   * extending the block by 4 if necessary.
   * Resize the index if the size threshold is reached.
   */
  private void insertNewKey(long[] block, int bucket, int idx, long key, T value)
  {
    int len = block.length;
    boolean newlength = false;
    long[] newblock;
    T[]  newvalue;
    T[]  entry = value_[bucket];

    if (block[len-1]==0) {
      newblock = block;
      newvalue = value_[bucket];
    }
    else {
      newlength = true;
      newblock = new long[len+4];
      newvalue = (T[]) new Object[len+4];
      System.arraycopy(block, 0, newblock, 0, idx);
      System.arraycopy(entry, 0, newvalue, 0, idx);
    }

    if (idx<len) {
      if (newlength) {
        System.arraycopy(block, idx, newblock, idx+1, len-idx);
        System.arraycopy(entry, idx, newvalue, idx+1, len-idx);
      }
      else {
        // if not newlength, the block ends with at least one 0 padding
        // which can be ignored
        System.arraycopy(block, idx, newblock, idx+1, len-idx-1);
        System.arraycopy(entry, idx, newvalue, idx+1, len-idx-1);
      }
    }
    newblock[idx] = key;
    newvalue[idx] = value;

    // FIXME?: not atomic
    table_[bucket] = newblock;
    value_[bucket] = newvalue;
    if (size_++ >= threshold_) {
      resize(2 * table_.length);
    }
  }

  /**
   * Insert a new key in a new block of size 4.
   */
  private final void insertNewBlock(int bucket, long key, T value)
  {
    long[] block = new long[4];
    T[]    entry = (T[]) new Object[4];
    block[0] = key;
    entry[0] = value;
    table_[bucket] = block;
    value_[bucket] = entry;

    // the purpose of resize is to reduce block size for binarySearch,
    // so we don't resize for new block
    size_++;
  }

  /**
   * Remove the specified key from the NativeHashObject.
   * Does not get the memory back, except if it's the last key of the block.
   * Returns whether it was there.
   */
  @Override
  public boolean remove(long key)
  {
    if (key==0) {
      if (contains_zero_) {
        contains_zero_ = false;
        return true;
      }
      else {
        return false;
      }
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
          T[] entry = value_[bucket];
          System.arraycopy(entry, idx+1, entry, idx, high-idx);
        }
        else {
          table_[bucket] = null;
          value_[bucket] = null;
        }
        size_--;
        return true;
      }
    }

    return false;
  }


  /**
   * Removes all of the mappings from this map.
   * The map will be empty after this call returns.
   */
  public void clear()
  {
    super.clear();
    for (int i = 0; i < value_.length; i++) {
      value_[i] = null;
    }
    value_zero_ = null;
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
  private void resize(int newCapacity)
  {
    long t0=0, t1;
    if (trace_) {
      System.err.println("[NativeHashObject] resizing index to "+newCapacity+", size="+size_);
      t0 = System.currentTimeMillis();
    }
    int oldCapacity = table_.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
      threshold_ = Integer.MAX_VALUE;
      return;
    }
    long[][] newTable = new long[newCapacity][];
    T[][]  newValue = (T[][]) new Object[newCapacity][];
    for (int i = 0; i < oldCapacity; i++) {
      long[] e = table_[i];
      T[]  v = value_[i];
      if (e != null) {
        int len = e.length;
        long[] e0 = new long[len];
        long[] e1 = new long[len];
        T[] v0 = (T[]) new Object[len];
        T[] v1 = (T[]) new Object[len];
        int i0=0;
        int i1=0;
        for (int j=0 ; j<len ; j++) {
          long key = e[j];
          T  value = v[j];
          if (key!=0) {
            int bucket = indexFor((int) key, newCapacity);
            if (bucket == i) {
              v0[i0] = value;
              e0[i0++] = key;
            }
            else {
              v1[i1] = value;
              e1[i1++] = key;
            }
          }
        }
        if (i0>0) {
          newTable[i] = e0;
          newValue[i] = v0;
        }
        if (i1>0) {
          newTable[i+oldCapacity] = e1;
          newValue[i+oldCapacity] = v1;
        }
      }
    }

    // WARNING: not atomic here
    table_ = newTable;
    value_ = newValue;

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
      System.err.println("[NativeHashObject] resizing time="+(t1-t0)+" ms, loadFactor="
                         +loadFactor_+" newCapacity="+newCapacity
                         +" threshold="+threshold_);
    }
  }

  @Override
  public Iterator iterator() {
    return nativeIterator();
  }

  public NativeHashObjectIterator nativeIterator() {
    return new NativeHashObjectIterator();
  }

  public final class NativeHashObjectIterator
  implements Iterator<Entry>
  {
    private int currBucketNum_ = 0;
    private int currCellNum_ = 0;
    private boolean handlingZero_ = false;
    private int currentCount_ = 0;

    private NativeHashObjectIterator() {}

    @Override
    public boolean hasNext()
    {
      //check for zero value in the end
      if (contains_zero_ && currentCount_ == size_-1) {
        handlingZero_ = true;
      }

      return (currentCount_ != size_);
    }

    public T nextValue()
    {
      //special code for handling zero
      if (handlingZero_) {
        currentCount_++;
        return value_zero_;
      }

      //skip over empty buckets
      while (table_[currBucketNum_] == null) {
        currBucketNum_++;
      }

      //read the actual key-value entry
      int numKeys = table_[currBucketNum_].length;
      T p = value_[currBucketNum_][currCellNum_];
      currCellNum_++;

      //check if we have reached the end of this block
      if (currCellNum_ >= numKeys || table_[currBucketNum_][currCellNum_]==0) {
        currBucketNum_++;
        currCellNum_ = 0;
      }

      currentCount_++;
      return p;
    }

    @Override
    public Entry next()
    {
      //special code for handling zero
      if (handlingZero_) {
        Entry p = new Entry(0L, value_zero_);
        currentCount_++;
        return p;
      }

      //skip over empty buckets
      while (currBucketNum_<table_.length && table_[currBucketNum_] == null) {
        currBucketNum_++;
      }

      //read the actual key-value entry
      int numKeys = table_[currBucketNum_].length;
      Entry p = new Entry(table_[currBucketNum_][currCellNum_], value_[currBucketNum_][currCellNum_]);
      currCellNum_++;

      //check if we have reached the end of this block
      if (currCellNum_ >= numKeys || table_[currBucketNum_][currCellNum_]==0) {
        currCellNum_ = 0;
        currBucketNum_++;
        //skip over empty buckets
        while (currBucketNum_<table_.length && table_[currBucketNum_] == null) {
          currBucketNum_++;
        }
      }

      currentCount_++;
      return p;
    }

    /**
     * Not implemented
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static class Entry<T>
  {
    public final long key;
    public final T    value;

    public Entry(long k, T v)
    {
      key = k;
      value = v;
    }
  }

  public String toString()
  {
    StringBuilder sb = null;
    if (contains_zero_) {
      sb = new StringBuilder("{0:")
           .append(value_zero_);
    }
    for (int i=0 ; i<table_.length ; i++) {
      long[] block = table_[i];
      T[]  vals = value_[i];
      if (block!=null) {
        for (int j=0 ; j<block.length ; j++) {
          long k = block[j];
          if (k!=0) {
            if (sb==null) {
              sb = new StringBuilder("{");
            }
            else {
              sb.append(", ");
            }
            sb.append(k)
              .append(':')
              .append(vals[j]);
          }
        }
      }
    }
    if (sb==null) return "{}";
    sb.append('}');
    return sb.toString();
  }
}
