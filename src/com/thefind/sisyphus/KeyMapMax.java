package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class KeyMapMax
extends OutputKeyMap
{
  protected final boolean isMax_;

  public KeyMapMax(KeyMap map, String col_max, boolean isMax)
  {
    super(map, new String[] { map.getSchemaIn().get(0), col_max});
    isMax_ = isMax;
  }

  @Override
  public void append(String[] that)
  {
    int newval = Integer.parseInt(that[1]);
    int oldval = map_.put(that[0], newval);
    if (isMax_) {
      if (newval<oldval) {
        map_.put(that[0], oldval);
      }
    }
    else if (newval>oldval) {
      map_.put(that[0], oldval);
    }
  }

  @Override
  public String toStringWhich()
  {
    List<String> sch = map_.getSchemaIn();
    return "["+sch.get(0)+", "+sch.get(1)+"=" + (isMax_?"Max(":"Min(") +schema_.get(1)+")]";
  }
}

