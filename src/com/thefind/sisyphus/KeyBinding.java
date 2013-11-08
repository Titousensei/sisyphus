package com.thefind.sisyphus;

import java.util.*;
import java.io.*;

import com.thefind.util.NativeHashBinding;
import com.thefind.util.NativeHash;
import com.thefind.util.NativeHash.NativeHashIterator;

import com.thefind.sisyphus.test.*;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class KeyBinding
extends Key
implements Serializable
{
  private static final long serialVersionUID = -998224046673333984L;

  public final static long NULL_BIND = 0L;

  protected final NativeHashBinding bind_;

  public KeyBinding(String keyname, String valuename)
  {
    super(keyname, valuename);

    bind_ = new NativeHashBinding();
    bind_.null_equivalent_ = NULL_BIND;
  }

  @Override
  protected NativeHash getNativeHash()
  { return bind_; }

  @Override
  public boolean add(long key)
  { return (bind_.put(key, 1) != bind_.null_equivalent_); }

  @Override
  public boolean contains(long key)
  { return bind_.contains(key); }

  @Override
  public NativeHashIterator keyIterator()
  { return bind_.keyIterator(); }

  @Override
  public void remove(long key)
  { bind_.remove(key); }

  @Override
  public void clear()
  { bind_.clear(); }

  public long put(long key, long value)
  { return bind_.put(key,value); }

  public boolean putIfNotPresent(long key, long value)
  { return bind_.putIfNotPresent(key,value); }

  public int contains(long key, long value)
  { return bind_.contains(key, value); }

  public long get(long key)
  { return bind_.get(key); }

  public Iterator<NativeHashBinding.Entry> iterator()
  { return bind_.iterator(); }

  /* KEY AS STRING */

  public long put(String key_str, long value)
  {
    try {
      return bind_.put(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return NULL_BIND;
    }
  }

  public boolean putIfNotPresent(String key_str, long value)
  {
    try {
      return bind_.putIfNotPresent(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return false;
    }
  }

  public int contains(String key_str, long value)
  {
    try {
      return bind_.contains(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return 0;
    }
  }

  public long get(String key_str)
  {
    try {
      return bind_.get(Long.parseLong(key_str));
    } catch (NumberFormatException nfex) {
      return NULL_BIND;
    }
  }

  @Override
  public String toStringDump()
  { return bind_.toString(); }

  public String toStringStats()
  { return bind_.toStringStats(); }

  @Override
  public int size()
  { return bind_.size(); }

  /* SERIALIZATION */

  public static KeyBinding newLike(KeyBinding like)
  { return new KeyBinding(like.getColumnName(0), like.getColumnName(1)); }

  public static KeyBinding load(String filename)
  throws LoadingException
  { return Key.load(KeyBinding.class, filename); }

  public static KeyBinding loadStrict(String filename, String keyname, String valuename)
  throws LoadingException, SchemaException
  {
    KeyBinding ret = load(filename);
    List<String> sch = ret.getSchemaIn();
    if (keyname.equals(sch.get(0)) && valuename.equals(sch.get(1))) {
      return ret;
    }
    else {
      throw new SchemaException("["+keyname+","+valuename+"] != "+sch);
    }
  }

  public static KeyBinding load(String filename, String keyname, String valuename)
  {
    try {
      return loadStrict(filename, keyname, valuename);
    }
    catch (SchemaException ex) {
      System.err.println("[KeyBinding.load] WARNING - Loaded schema does not match expected schema: "+ex.getMessage());
    }
    catch (LoadingException ex) {
      System.err.println("[KeyBinding.load] WARNING - Could not load "+filename+": "+ex.getMessage());
    }
    return new KeyBinding(keyname, valuename);
  }

  public void dumpTsv(PrintStream out)
  {
    Iterator<NativeHashBinding.Entry> it = bind_.iterator();
    int line_count = 0;
    while (it.hasNext()) {
      ++ line_count;
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
      NativeHashBinding.Entry e = it.next();
      out.print(e.key);
      out.print('\t');
      out.print(e.value);
      out.println();
    }
    System.err.println("DONE "+line_count);
  }

  public static KeyBinding loadKeyBindingFromTsv(String filepath, String colkey, String colvalue)
  {
    KeyBinding ret = new KeyBinding(colkey, colvalue);

    Input in_key = new InputFile(filepath, colkey, colvalue);
    new Pusher()
        .always(new OutputKeyBinding(ret))
        .push(in_key);

    System.err.println("[KeyBinding.loadFromFile] loaded: "+filepath+" -> "+ret.toString());
    return ret;
  }

  public static void main(String[] args)
  {
    KeyBinding bind;
    if ("-tsv".equals(args[0])) {
      bind = loadKeyBindingFromTsv(args[1], args[2], args[3]);
      bind.save(args[4]);
      return;
    }
    else {
      try {
        bind = KeyBinding.load(args[0]);
      }
      catch (LoadingException ex) {
        throw new RuntimeException(ex);
      }
    }

    if ("-value".equals(args[1])) {
      KeyBinding ret = KeyBinding.newLike(bind);
      Output out = new OutputKeyBinding(ret);

      Pusher p = new Pusher();
      for (int i=2; i<args.length ; i++) {
        p.onlyIf(new IfEquals(bind.getColumnName(1), args[i]), out);
      }
      p.push(new InputKeyBinding(bind));
      ret.dumpTsv(System.out);
    }
    else if ("-count".equals(args[1])) {
      Pusher p = new Pusher();
      for (int i=2; i<args.length ; i++) {
        p.onlyIf(new IfEquals(bind.getColumnName(1), args[i]), new BreakAfter(args[i]));
      }
      p.push(new InputKeyBinding(bind));
    }
    else if ("-dump".equals(args[1])) {
      bind.dumpTsv(System.out);
    }
    else if ("-prompt".equals(args[1])) {
      try {
        jline.ConsoleReader reader = new jline.ConsoleReader();
        reader.setBellEnabled(false);
        reader.getHistory().setHistoryFile(new File(".KeyBinding.history"));
        String line = null;
        System.err.println("=== Enter Keys ===");
        while ((line = reader.readLine("keys> ")) != null) {
          if (!line.trim().isEmpty()) {
            for (String k : line.split("\\s+")) {
              System.out.print(k);
              System.out.print('\t');
              if (bind.contains(k)) {
                System.out.print(bind.get(k));
              }
              else {
                System.out.print("false");
              }
              System.out.println();
            }
          }
        }
      }
      catch(IOException ioex) {
        ioex.printStackTrace();
      }
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
          System.out.print(bind.get(args[i]));
        }
        else {
          System.out.print("false");
        }
        System.out.println();
      }
    }
  }
}

