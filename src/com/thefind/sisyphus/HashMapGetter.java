package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class HashMapGetter
extends Modifier
{
  protected final Map<String, String> map_;

  public HashMapGetter(Map<String, String> map, String schema_key, String schema_value)
  {
    super(new String[] { schema_key }, new String[] { schema_value });
    map_ = map;
  }

  public void compute(String[] input, String[] result)
  { result[0] = map_.get(input[0]); }
}

