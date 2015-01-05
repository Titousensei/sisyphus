package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public final class KeyStringSetter
extends OutputKeyString
{
  private final String value_;

  public KeyStringSetter(KeyString kstring, String value)
  {
    super(kstring, kstring.getSchemaIn().get(0));
    value_ = value;
  }

  @Override
  public void append(String[] column)
  { kstring_.put(column[0], value_); }

  @Override
  public String toStringWhich()
  { return "["+schema_.get(0)+","+value_+"]"; }
}

