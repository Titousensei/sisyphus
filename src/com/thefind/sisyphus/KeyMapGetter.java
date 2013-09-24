package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */ 
public class KeyMapGetter
extends Modifier
{
  protected final KeyMap map_;

  public KeyMapGetter(KeyMap map)
  {
    super(new String[] { map.getSchemaIn().get(0) }, new String[] { map.getSchemaIn().get(1) });
    map_ = map;
  }

  /**
   * Use this constructor only if the query schema doesn't match the KeyMap schema.
   * This is for the rare case where you have several key columns with different names,
   * and you want to populate correspoding value columns, also with different names.
   */
  public KeyMapGetter(KeyMap map, String col_key, String col_value)
  {
    super(new String[] { col_key }, new String[] { col_value });
    map_ = map;
  }

  @Override
  public void compute(String[] input, String[] result)
  {
    try {
      long key = Long.parseLong(input[0]);
      int value = map_.get(key);
      if ((value != KeyMap.NULL_INT) || (map_.contains(key))) {
        result[0] = Integer.toString(value);
      }
    } catch (NumberFormatException nfex) {
      result[0] = null;
    }
  }
}

