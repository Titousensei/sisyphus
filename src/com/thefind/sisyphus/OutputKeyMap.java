package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class OutputKeyMap
extends Output
{
  protected final KeyMap map_;

  public OutputKeyMap(KeyMap map)
  {
    super(map.getSchemaIn());
    map_ = map;
  }

  public OutputKeyMap(KeyMap map, String... schema)
  {
    super(schema);
    map_ = map;
  }

  @Override
  protected void append(String[] values)
  {
    try {
      if (values[0]==null || "".equals(values[0])) { return; }
      if (values[1]==null || "".equals(values[1])) { return; }

      long key = Long.parseLong(values[0]);
      int val  = Integer.parseInt(values[1]);

      map_.put(key, val);
    }
    catch (NumberFormatException nfex) {
      warnings_ ++;
      if (warnings_<10) {
        System.out.println("[OutputKeyMap] WARNING - NumberFormatException "
            + Arrays.toString(values) + " in " + map_.toString());
      }
      return;
    }
  }

  @Override
  public boolean open() { return true; }

  @Override
  public void close() {}

  @Override
  public boolean sameAs(long that)
  { return (that==map_.hashCode()); }

  @Override
  protected String toStringWhich()
  { return map_.toString(); }
}

