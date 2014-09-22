package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.NativeHashLookup;

/**
 * @author Eric Gaudet
 */
public class InputKeyMap
extends Input
{
  protected final KeyMap map_;
  protected final String[] container_;

  protected Iterator<NativeHashLookup.Entry> iterator_ = null;

  public InputKeyMap(KeyMap src)
  {
    super(src.getSchemaOut());
    map_ = src;
    container_ = new String[2];
  }

  public int size()
  { return map_.size(); }

  @Override
  public boolean open()
  {
    if (iterator_!=null) {
      return false;
    }

    iterator_ = map_.iterator();

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

    NativeHashLookup.Entry entry = iterator_.next();
    result[0] = Long.toString(entry.key);
    result[1] = Integer.toString(entry.value);
    return 2;
  }

  @Override
  public long getInternalHashCode()
  { return map_.hashCode(); }

  @Override
  protected String toStringWhich()
  { return ""; }
}

