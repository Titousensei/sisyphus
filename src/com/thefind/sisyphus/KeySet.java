package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.NativeHash;
import com.thefind.util.NativeHash.NativeHashIterator;
import com.thefind.util.NativeHashSet;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class KeySet
extends Key
implements Serializable
{
  private static final long serialVersionUID = -2492841252572797902L;

  protected NativeHashSet set_;

  public KeySet(String keyname)
  { this(keyname, new NativeHashSet()); }

  public KeySet(String keyname, NativeHashSet set)
  {
    super(keyname);
    set_ = set;
  }

  @Override
  protected NativeHash getNativeHash()
  { return set_; }

  public boolean add(long key)
  { return set_.add(key); }

  @Override
  public NativeHashIterator keyIterator()
  { return set_.keyIterator(); }

  @Override
  public boolean contains(long key)
  { return set_.contains(key); }

  @Override
  public String toStringDump()
  { return set_.toString(); }

  public String toStringStats()
  { return set_.toStringStats(); }

  @Override
  public int size()
  { return set_.size(); }

  @Override
  public void remove(long key)
  { set_.remove(key); }

  @Override
  public void clear()
  { set_.clear(); }

  public static KeySet newLike(Key like)
  { return new KeySet(like.getColumnName(0)); }

  public static KeySet newLikeValue(KeyMap like)
  { return new KeySet(like.getColumnName(1)); }

  public static KeySet newLikeValue(KeyBinding like)
  { return new KeySet(like.getColumnName(1)); }

  /* SERIALIZATION */

  public static KeySet load(String filename)
  throws LoadingException
  { return Key.load(KeySet.class, filename); }

  public static KeySet loadStrict(String filename, String keyname)
  throws LoadingException, SchemaException
  {
    KeySet ret = load(filename);
    List<String> sch = ret.getSchemaIn();
    if (keyname.equals(sch.get(0))) {
      return ret;
    }
    else {
      throw new SchemaException("["+keyname+"] != "+sch);
    }
  }

  public static KeySet load(String filename, String keyname)
  {
    try {
      return loadStrict(filename, keyname);
    }
    catch (SchemaException ex) {
      System.err.println("[KeyMap.load] WARNING - Loaded schema does not match expected schema: "+ex.getMessage());
    }
    catch (LoadingException ex) {
      System.err.println("[KeyMap.load] WARNING - Could not load "+filename+": "+ex.getMessage());
    }
    return new KeySet(keyname);
  }

/*
  public void dumpTsv(String filename) throws IOException
  {
    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
    NativeHashIterator it = set_.keyIterator();
    while (it.hasNext()) {
      writer.write(it.nextKey() +"\n");
    }
    writer.close();
  }
*/

  @Override
  public void dumpTsv(PrintStream out)
  {
    NativeHashIterator it = set_.keyIterator();
    while (it.hasNext()) {
      out.println(it.nextKey());
    }
  }

  public static void main(String[] args)
  {
    KeySet map;
    try {
      map = KeySet.load(args[0]);
    }
    catch (LoadingException ex) {
      throw new RuntimeException(ex);
    }

    int i=1;
    if ("-key".equals(args[1])) {
      i=2;
      for (; i<args.length ; i++) {
        System.out.print(args[i]);
        System.out.print('\t');
        if (map.contains(args[i])) {
          System.out.print("true");
        }
        else {
          System.out.print("false");
        }
        System.out.println();
      }
    }
    else if ("-dump".equals(args[1])) {
      map.dumpTsv(System.out);
    }
  }
}

