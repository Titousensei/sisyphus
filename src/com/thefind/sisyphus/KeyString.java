package com.thefind.sisyphus;

import java.util.*;
import java.io.*;

import com.thefind.util.NativeHashObject;
import com.thefind.util.NativeHash;
import com.thefind.util.NativeHash.NativeHashIterator;

import com.thefind.sisyphus.test.*;

/**
 * @author Eric Gaudet
 */
public class KeyString
extends Key
implements Serializable
{
  private static final long serialVersionUID = 5319629579877914478L;

  protected final NativeHashObject<String> kstring_;

  public KeyString(String keyname, String valuename)
  {
    super(keyname, valuename);

    kstring_ = new NativeHashObject();
  }

  @Override
  protected NativeHash getNativeHash()
  { return kstring_; }

  @Override
  public boolean add(long key)
  { return kstring_.put(key, "")!=null; }

  @Override
  public boolean contains(long key)
  { return kstring_.contains(key); }

  @Override
  public NativeHashIterator keyIterator()
  { return kstring_.keyIterator(); }

  @Override
  public void remove(long key)
  { kstring_.remove(key); }

  @Override
  public void clear()
  { kstring_.clear(); }

  public String put(long key, String value)
  { return kstring_.put(key,value); }

  public boolean putIfNotPresent(long key, String value)
  { return kstring_.putIfNotPresent(key,value); }

  /* append? substring?
  public double increment(long key, double value)
  { return kstring_.increment(key, value); }

  public double multiply(long key, double value)
  { return kstring_.multiply(key, value); }
  */

  public int contains(long key, String value)
  { return kstring_.contains(key, value); }

  public String get(long key)
  { return kstring_.get(key); }

  public Iterator<NativeHashObject.Entry<String>> iterator()
  { return kstring_.iterator(); }

  /* KEY AS STRING */

  public String put(String key_str, String value)
  {
    try {
      return kstring_.put(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return null;
    }
  }

  public boolean putIfNotPresent(String key_str, String value)
  {
    try {
      return kstring_.putIfNotPresent(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return false;
    }
  }

  public int contains(String key_str, String value)
  {
    try {
      return kstring_.contains(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return 0;
    }
  }

  public String get(String key_str)
  {
    try {
      return kstring_.get(Long.parseLong(key_str));
    } catch (NumberFormatException nfex) {
      return null;
    }
  }

  @Override
  public String toStringDump()
  { return kstring_.toString(); }

  public String toStringStats()
  { return kstring_.toStringStats(); }

  @Override
  public int size()
  { return kstring_.size(); }

  /* SERIALIZATION */

  public static KeyString newLike(KeyString like)
  { return new KeyString(like.getColumnName(0), like.getColumnName(1)); }

  public static KeyString load(String filename)
  throws LoadingException
  { return Key.load(KeyString.class, filename); }

  public static KeyString loadStrict(String filename, String keyname, String valuename)
  throws LoadingException, SchemaException
  {
    KeyString ret = load(filename);
    List<String> sch = ret.getSchemaIn();
    if (keyname.equals(sch.get(0)) && valuename.equals(sch.get(1))) {
      return ret;
    }
    else {
      throw new SchemaException("["+keyname+","+valuename+"] != "+sch);
    }
  }

  public static KeyString load(String filename, String keyname, String valuename)
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
    return new KeyString(keyname, valuename);
  }

  public void dumpTsv(PrintStream out)
  {
    Iterator<NativeHashObject.Entry<String>> it = kstring_.iterator();
    int line_count = 0;
    while (it.hasNext()) {
      if ((line_count % 1000000) == 0) {
        System.err.println("... "+(line_count/1000000)+"M");
      }
      else if ( (line_count<1000000)
      && ((line_count == 300000) || (line_count == 100000)
         || (line_count == 30000)  || (line_count == 10000)
         || (line_count == 3000)   || (line_count == 1000))
      ) {
        System.err.println("... "+(line_count/1000)+"K");
      }
      NativeHashObject.Entry<String> e = it.next();
      out.print(e.key);
      out.print('\t');
      out.print(e.value);
      out.println();
      ++ line_count;
    }
    System.err.println("DONE "+line_count);
  }

  public static KeyString loadKeyStringFromTsv(String filepath, String colkey, String colvalue)
  {
    KeyString ret = new KeyString(colkey, colvalue);

    Input in_key = new InputFile(filepath, colkey, colvalue);
    new Pusher("loadKeyStringFromTsv")
        .always(new OutputKeyString(ret))
        .push(in_key);

    System.err.println("[KeyString.loadFromFile] loaded: "+filepath+" -> "+ret.toString());
    return ret;
  }

  public static void main(String[] args)
  {
    KeyString bind;
    if ("-tsv".equals(args[0])) {
      bind = loadKeyStringFromTsv(args[1], args[2], args[3]);
      bind.save(args[4]);
      return;
    }
    else {
      try {
        bind = KeyString.load(args[0]);
      }
      catch (LoadingException ex) {
        throw new RuntimeException(ex);
      }
    }

    if ("-value".equals(args[1])) {
      KeyString ret = KeyString.newLike(bind);
      Output out = new OutputKeyString(ret);

      Pusher p = new Pusher();
      for (int i=2; i<args.length ; i++) {
        p.onlyIf(new IfEquals(bind.getColumnName(1), args[i]), out);
      }
      p.push(new InputKeyString(bind));
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
      p.push(new InputKeyString(bind));
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

