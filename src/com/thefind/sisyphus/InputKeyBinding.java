package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.NativeHashBinding;

/**
 * @author Eric Gaudet
 */
public class InputKeyBinding
extends Input
{
  protected final KeyBinding bind_;
  protected final String[] container_;

  public Iterator<NativeHashBinding.Entry> iterator_ = null;

  public InputKeyBinding(KeyBinding src)
  {
    super(src.getSchemaOut());
    bind_ = src;
    container_ = new String[2];
  }

  public int size()
  { return bind_.size(); }

  @Override
  public boolean open()
  {
    if (iterator_!=null) { return false; }

    iterator_ = bind_.iterator();

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

    NativeHashBinding.Entry entry = iterator_.next();
    result[0] = Long.toString(entry.key);
    result[1] = Long.toString(entry.value);
    return 2;
  }

  @Override
  protected long getInternalHashCode()
  { return bind_.hashCode(); }

  @Override
  protected String toStringWhich()
  { return ""; }
}

