package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.NativeHashDouble;

/**
 * @author Eric Gaudet
 */
public class InputKeyDouble
extends Input
{
  protected final KeyDouble double_;
  protected final String[] container_;

  public Iterator<NativeHashDouble.Entry> iterator_ = null;

  public InputKeyDouble(KeyDouble src)
  {
    super(src.getSchemaOut());
    double_ = src;
    container_ = new String[2];
  }

  public int size()
  { return double_.size(); }

  @Override
  public boolean open()
  {
    if (iterator_!=null) {
      return false;
    }

    iterator_ = double_.iterator();

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

    NativeHashDouble.Entry entry = iterator_.next();
    result[0] = Long.toString(entry.key);
    result[1] = Double.toString(entry.value);
    return 2;
  }

  @Override
  protected long getInternalHashCode()
  { return double_.hashCode(); }

  @Override
  protected String toStringWhich()
  { return ""; }
}

