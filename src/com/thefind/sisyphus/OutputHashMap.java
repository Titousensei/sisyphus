package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public class OutputHashMap
extends Output
{
  protected final Map map_;

  public OutputHashMap(Map<?,?> map, String schema_key, String schema_value)
  {
    super(new String[] { schema_key, schema_value });
    map_ = map;
  }

  @Override
  protected void append(String[] values)
  { map_.put(StringUtil.canonicalize(values[0]), StringUtil.canonicalize(values[1])); }

  @Override
  public boolean open() { return true; }

  @Override
  public void close() {}

  @Override
  public boolean sameAs(long that)
  { return (that==map_.hashCode()); }

  @Override
  protected String toStringWhich()
  { return String.format("%,d entries", map_.size()); }
}

