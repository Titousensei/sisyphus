package com.thefind.util;

import java.util.*;

/**
 * An LRU cache, extending LinkedHashMap, fully implementing the Map interface.
 * This cache has a fixed maximum number of elements (cacheSize).
 * If the cache is full and another entry is added:
 *   - the LRU (least recently used) entry is dropped,
 *   - onRemove is called for this entry (useful to close resources)
 *
 * @author Eric Gaudet
 */
public class LruMap<K,V>
extends LinkedHashMap<K,V>
{
  private final int cacheSize_;

  /**
   * Creates a new LRU cache.
   * @param cacheSize the maximum number of entries that will be kept in this cache.
   */
  public LruMap(int cacheSize)
  {
    // initialCapacity + 1 because the eldestEntry will be present, then deleted
    // loadFactor 1.1f to prevent unnecessary re-hashing, since the size will not change
    // accessOrder=true, as opposed to insertOrder
    super(cacheSize + 1, 1.1f, true);
    cacheSize_ = cacheSize;
  }

  @Override
  public V remove(Object key)
  {
    V value = super.remove(key);
    if (value!=null) {
      onRemove((K) key, value);
    }
    return value;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K,V> eldest)
  {
    if (size() > cacheSize_) {
      onRemove(eldest.getKey(), eldest.getValue());
      return true;
    }
    return false;
  }

  /**
   * Override to do something when an entry is to be removed
   */
  protected void onRemove(K key, V value) {}

  public int capacity()
  { return cacheSize_; }
}

