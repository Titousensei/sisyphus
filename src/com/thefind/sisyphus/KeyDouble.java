package com.thefind.sisyphus;

import java.util.*;
import java.io.*;

import com.thefind.util.NativeHashDouble;
import com.thefind.util.NativeHash;
import com.thefind.util.NativeHash.NativeHashIterator;

import com.thefind.sisyphus.test.*;

/**
 * @author Eric Gaudet
 */
public class KeyDouble
extends Key
implements Serializable
{
  private static final long serialVersionUID = 5319629509877914478L;

  protected final NativeHashDouble kdouble_;

  public KeyDouble(String keyname, String valuename)
  {
    super(keyname, valuename);

    kdouble_ = new NativeHashDouble();
  }

  @Override
  protected NativeHash getNativeHash()
  { return kdouble_; }

  @Override
  public boolean add(long key)
  { return !Double.isNaN(kdouble_.put(key, 1.0)); }

  @Override
  public boolean contains(long key)
  { return kdouble_.contains(key); }

  @Override
  public NativeHashIterator keyIterator()
  { return kdouble_.keyIterator(); }

  @Override
  public void remove(long key)
  { kdouble_.remove(key); }

  @Override
  public void clear()
  { kdouble_.clear(); }

  public double put(long key, double value)
  { return kdouble_.put(key,value); }

  public boolean putIfNotPresent(long key, double value)
  { return kdouble_.putIfNotPresent(key,value); }

  public double increment(long key, double value)
  { return kdouble_.increment(key, value); }

  public double multiply(long key, double value)
  { return kdouble_.multiply(key, value); }

  public int contains(long key, double value)
  { return kdouble_.contains(key, value); }

  public double get(long key)
  { return kdouble_.get(key); }

  public Iterator<NativeHashDouble.Entry> iterator()
  { return kdouble_.iterator(); }

  /* KEY AS STRING */

  public double put(String key_str, double value)
  {
    try {
      return kdouble_.put(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return Double.NaN;
    }
  }

  public boolean putIfNotPresent(String key_str, double value)
  {
    try {
      return kdouble_.putIfNotPresent(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return false;
    }
  }

  public int contains(String key_str, double value)
  {
    try {
      return kdouble_.contains(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return 0;
    }
  }

  public double get(String key_str)
  {
    try {
      return kdouble_.get(Long.parseLong(key_str));
    } catch (NumberFormatException nfex) {
      return Double.NaN;
    }
  }

  @Override
  public String toStringDump()
  { return kdouble_.toString(); }

  public String toStringStats()
  { return kdouble_.toStringStats(); }

  @Override
  public int size()
  { return kdouble_.size(); }

  /* SERIALIZATION */

  public static KeyDouble newLike(KeyDouble like)
  { return new KeyDouble(like.getColumnName(0), like.getColumnName(1)); }

  public static KeyDouble load(String filename)
  throws LoadingException
  { return Key.load(KeyDouble.class, filename); }

  public static KeyDouble loadStrict(String filename, String keyname, String valuename)
  throws LoadingException, SchemaException
  {
    KeyDouble ret = load(filename);
    List<String> sch = ret.getSchemaIn();
    if (keyname.equals(sch.get(0)) && valuename.equals(sch.get(1))) {
      return ret;
    }
    else {
      throw new SchemaException("["+keyname+","+valuename+"] != "+sch);
    }
  }

  public static KeyDouble load(String filename, String keyname, String valuename)
  {
    try {
      return loadStrict(filename, keyname, valuename);
    }
    catch (SchemaException ex) {
      System.err.println("[KeyDouble.load] WARNING - Loaded schema does not match expected schema: "+ex.getMessage());
    }
    catch (LoadingException ex) {
      System.err.println("[KeyDouble.load] WARNING - Could not load "+filename+": "+ex.getMessage());
    }
    return new KeyDouble(keyname, valuename);
  }

  public void dumpTsv(PrintStream out)
  {
    Iterator<NativeHashDouble.Entry> it = kdouble_.iterator();
    while (it.hasNext()) {
      NativeHashDouble.Entry e = it.next();
      out.print(e.key);
      out.print('\t');
      out.print(e.value);
      out.println();
    }
  }

  public static void main(String[] args)
  {
    KeyDouble bind;
    try {
      bind = KeyDouble.load(args[0]);
    }
    catch (LoadingException ex) {
      throw new RuntimeException(ex);
    }

    if ("-value".equals(args[1])) {
      KeyDouble ret = KeyDouble.newLike(bind);
      Output out = new OutputKeyDouble(ret);

      Pusher p = new Pusher();
      for (int i=2; i<args.length ; i++) {
        p.onlyIf(new IfEquals(bind.getColumnName(1), args[i]), out);
      }
      p.push(new InputKeyDouble(bind));
      ret.dumpTsv(System.out);
    }
    else if ("-dump".equals(args[1])) {
      bind.dumpTsv(System.out);
    }
    else if ("-count".equals(args[1])) {
      Pusher p = new Pusher();
      for (int i=2; i<args.length ; i++) {
        p.onlyIf(new IfEquals(bind.getColumnName(1), args[i]), new BreakAfter(args[i]));
      }
      p.push(new InputKeyDouble(bind));
    }
    else {
      int i=1;
      if ("-key".equals(args[1])) {
        i=2;
      }
      for (; i<args.length ; i++) {
        System.out.print(args[i]);
        System.out.print('\t');
        if (bind.contains(args[i])) {
          System.err.print(bind.get(args[i]));
        }
        else {
          System.out.print("false");
        }
        System.out.println();
      }
    }
  }
}

