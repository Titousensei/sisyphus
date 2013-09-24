package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public final class KeyDeleter
extends OutputKey
{
  public KeyDeleter(Key key)
  { super(key); }

  @Override
  public void append(String[] column)
  {
    if (column.length>0) {
      key_.remove(column[0]);
    }
  }

  @Override
  public boolean sameAs(long that)
  {
    if (super.sameAs(that)) {
      throw new ConcurrentModificationException("Direct deletion in input KeyMap: "+toString());
    }
    return false;
  }
}

