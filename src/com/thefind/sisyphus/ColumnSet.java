package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class ColumnSet
extends Modifier
{
  protected final String value_;

  public ColumnSet(String outcol, int value)
  { this(outcol, Integer.toString(value)); }

  public ColumnSet(String outcol, String value)
  {
    super(new String[] { outcol });
    value_ = (value!=null) ? value : "";
  }

  public void compute(String[] input, String[] result)
  { result[0] = value_; }

  @Override
  public String toString()
  { return getClass().getSimpleName()+"{ -> "+getSchemaOut()+" = \""+value_+"\"}"; }
}
