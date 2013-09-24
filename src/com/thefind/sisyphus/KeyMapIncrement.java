package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class KeyMapIncrement
extends OutputKeyMap
{
  protected final int value_;

  public KeyMapIncrement(KeyMap map, int value)
  {
    super(map, map.getSchemaIn().get(0));
    value_ = value;
  }

  @Override
  public void append(String[] that)
  { map_.increment(that[0], value_); }

  @Override
  public String toStringWhich()
  {
    List<String> sch = map_.getSchemaIn();
    return "["+sch.get(0)+", "+sch.get(1)+"+="+value_+"]";
  }
}

