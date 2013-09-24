package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.NativeHash.NativeHashIterator;

/**
 * @author Eric Gaudet
 */
public class InputKey
extends Input
{
  protected final Key key_;
  protected final String[] container_;

  public NativeHashIterator iterator_ = null;

  public InputKey(Key src)
  {
    super(src.getSchemaOut());
    key_ = src;
    container_ = new String[1];
  }

  public Key getKey()
  { return key_; }

  public int size()
  { return key_.size(); }

  @Override
  public boolean open()
  {
    if (iterator_!=null) {
      return false;
    }

    iterator_ = key_.keyIterator();

    return true;
  }

  @Override
  public void close()
  { iterator_ = null; }

  @Override
  protected int readRow(String[] result)
  {
    if (!iterator_.hasNext()) {
      close();
      return -1;
    }

    long k = iterator_.nextKey();
    result[0] = Long.toString(k);
    return 1;
  }

  @Override
  protected long getInternalHashCode()
  { return key_.hashCode(); }

  @Override
  protected String toStringWhich()
  { return ""; }
}

