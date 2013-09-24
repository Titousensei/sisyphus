package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public final class KeyMapSetter
extends OutputKeyMap
{
  private final int value_;

  public KeyMapSetter(KeyMap map, int value)
  {
    super(map, map.getSchemaIn().get(0));
    value_ = value;
  }

  @Override
  public void append(String[] column)
  {
    if (column.length>0) {
      map_.put(column[0], value_);
    }
  }

  @Override
  public String toStringWhich()
  { return "["+schema_.get(0)+","+value_+"]"; }
}

