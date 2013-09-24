package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.NativeHash;
import com.thefind.util.NativeHash.NativeHashIterator;

/**
 * @author Eric Gaudet
 */
public class KeySetAlways
extends Key
{
  public KeySetAlways(String keyname)
  { super(keyname); }

  @Override
  protected NativeHash getNativeHash()
  { throw new UnsupportedOperationException("Infinite list"); }

  public boolean add(long key)
  { return true; }

  @Override
  public NativeHashIterator keyIterator()
  { throw new UnsupportedOperationException("Can't iterate an infinite list"); }

  @Override
  public boolean contains(long key)
  { return true; }

  @Override
  public String toStringDump()
  { return "[<everything>]"; }

  public String toStringStats()
  { return "<no stats>"; }

  @Override
  public int size()
  { return 0; }

  @Override
  public void remove(long key)
  { }

  @Override
  public void clear()
  { }

  @Override
  public void dumpTsv(PrintStream out)
  { out.println("n/a"); }

  public static KeySetAlways newLike(Key like)
  { return new KeySetAlways(like.getColumnName(0)); }

  public static KeySetAlways newLikeValue(KeyMap like)
  { return new KeySetAlways(like.getColumnName(1)); }

  public static KeySetAlways newLikeValue(KeyBinding like)
  { return new KeySetAlways(like.getColumnName(1)); }
}

