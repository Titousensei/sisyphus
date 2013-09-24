package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class HashMapSetter
extends Modifier
{
  protected final Map<String, String> map_;

  public HashMapSetter(Map<String, String> map, String schema_key, String schema_value)
  {
    super(new String[] { schema_key, schema_value }, new String[0]);
    map_ = map;
  }

  @Override
  public void compute(String[] input, String[] result)
  throws Exception
  { map_.put(input[0], input[1]); }

  public String get(String key)
  { return map_.get(key); }

}

