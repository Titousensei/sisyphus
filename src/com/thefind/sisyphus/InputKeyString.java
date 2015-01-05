package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.NativeHashObject;

/**
 * @author Eric Gaudet
 */
public class InputKeyString
extends Input
{
  protected final KeyString kstring_;
  protected final String[] container_;

  protected Iterator<NativeHashObject.Entry<String>> iterator_ = null;

  public InputKeyString(KeyString src)
  {
    super(src.getSchemaOut());
    kstring_ = src;
    container_ = new String[2];
  }

  public int size()
  { return kstring_.size(); }

  @Override
  public boolean open()
  {
    if (iterator_!=null) {
      return false;
    }

    iterator_ = kstring_.iterator();

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

    NativeHashObject.Entry<String> entry = iterator_.next();
    result[0] = Long.toString(entry.key);
    result[1] = entry.value;
    return 2;
  }

  @Override
  public long getInternalHashCode()
  { return kstring_.hashCode(); }

  @Override
  protected String toStringWhich()
  { return ""; }
}

