package com.thefind.sisyphus;

import java.util.*;
import java.io.*;

import com.thefind.util.NativeHashLookup;
import com.thefind.util.NativeHash;
import com.thefind.util.NativeHash.NativeHashIterator;

import com.thefind.sisyphus.test.*;

/**
 * @author Eric Gaudet
 */
public class KeyMap
extends Key
implements Serializable
{
  private static final long serialVersionUID = -3863255086054500962L;

  public final static int NULL_INT = 0;

  protected final NativeHashLookup map_;

  public KeyMap(String keyname, String valuename)
  {
    super(keyname, valuename);

    map_ = new NativeHashLookup();
    map_.null_equivalent_ = NULL_INT;
  }

  @Override
  protected NativeHash getNativeHash()
  { return map_; }

  @Override
  public boolean add(long key)
  { return (map_.put(key, 1) != map_.null_equivalent_); }

  @Override
  public boolean contains(long key)
  { return map_.contains(key); }

  @Override
  public NativeHashIterator keyIterator()
  { return map_.keyIterator(); }

  @Override
  public void remove(long key)
  { map_.remove(key); }

  @Override
  public void clear()
  { map_.clear(); }

  public int put(long key, int value)
  { return map_.put(key,value); }

  public boolean putIfNotPresent(long key, int value)
  { return map_.putIfNotPresent(key,value); }

  public int increment(long key, int value)
  { return map_.increment(key, value); }

  public int contains(long key, int value)
  { return map_.contains(key, value); }

  public int get(long key)
  { return map_.get(key); }

  public Iterator<NativeHashLookup.Entry> iterator()
  { return map_.iterator(); }

  public static KeyMap newLike(KeyMap like)
  { return new KeyMap(like.getColumnName(0), like.getColumnName(1)); }

  /* UTILITIES */

  public KeyMap countValues(KeyMap src)
  {
    String val0 = src.getSchemaOut().get(1);
    String key1 = getSchemaIn().get(0);
    if (!val0.equals(key1)) {
      throw new SchemaException("Destination key \""+key1+"\" must be the same as source value \""+val0+"\"");
    }

    System.err.println("[KeyMap.countValues] --- "+src.getSchemaOut()+" -> "+getSchemaIn());
    new Pusher("countValues KeyMap")
        .always(new KeyMapIncrement(this, 1))
        .push(new InputKeyMap(src));

    return this;
  }

  public KeyMap countValues(KeyBinding src)
  {
    String val0 = src.getSchemaOut().get(1);
    String key1 = getSchemaIn().get(0);
    if (!val0.equals(key1)) {
      throw new SchemaException("Destination key \""+key1+"\" must be the same as source value \""+val0+"\"");
    }

    System.err.println("[KeyBinding.countValues] --- "+src.getSchemaOut()+" -> "+getSchemaIn());
    new Pusher("countValues KeyBinding")
        .always(new KeyMapIncrement(this, 1))
        .push(new InputKeyBinding(src));

    return this;
  }

  public KeyMap filterExclude(Test tst)
  {
    Key rows_remove = new KeySet(schema_.get(0));
    Input in_rows   = new InputKeyMap(this);

    new Pusher("filterExclude find")
        .onlyIf(tst, new OutputKey(rows_remove))
        .push(in_rows);

    int num_remove = rows_remove.size();
    if (num_remove>0) {
      new Pusher("filterExclude remove")
          .always(new KeyDeleter(this))
          .push(new InputKey(rows_remove));
    }

    return this;
  }

  public KeyMap filterOnly(Test tst)
  {
    Key rows_remove = new KeySet(schema_.get(0));
    Input in_rows   = new InputKeyMap(this);

    new Pusher("filterOnly find")
        .onlyIf(tst, BreakAfter.NO_OP)
        .always(new OutputKey(rows_remove))
        .push(in_rows);

    int num_remove = rows_remove.size();
    if (num_remove>0) {
      new Pusher("filterOnly remove")
          .always(new KeyDeleter(this))
          .push(new InputKey(rows_remove));
    }

    return this;
  }

  /* KEY AS STRING */

  public int increment(String key_str, int value)
  {
    try {
      return map_.increment(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return NULL_INT;
    }
  }

  public int contains(String key_str, int value)
  {
    try {
      return map_.contains(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return 0;
    }
  }

  public int put(String key_str, int value)
  {
    try {
      return map_.put(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return NULL_INT;
    }
  }

  public boolean putIfNotPresent(String key_str, int value)
  {
    try {
      return map_.putIfNotPresent(Long.parseLong(key_str), value);
    } catch (NumberFormatException nfex) {
      return false;
    }
  }

  public int get(String key_str)
  {
    try {
      return map_.get(Long.parseLong(key_str));
    } catch (NumberFormatException nfex) {
      return NULL_INT;
    }
  }

  @Override
  public String toStringDump()
  { return map_.toString(); }

  public String toStringStats()
  { return map_.toStringStats(); }

  @Override
  public int size()
  { return map_.size(); }

  /* SERIALIZATION */

  public static KeyMap load(String filename)
  throws LoadingException
  { return Key.load(KeyMap.class, filename); }

  public static KeyMap loadStrict(String filename, String keyname, String valuename)
  throws LoadingException, SchemaException
  {
    KeyMap ret = load(filename);
    List<String> sch = ret.getSchemaIn();
    if (keyname.equals(sch.get(0)) && valuename.equals(sch.get(1))) {
      return ret;
    }
    else {
      throw new SchemaException("["+keyname+","+valuename+"] != "+sch);
    }
  }

  public static KeyMap load(String filename, String keyname, String valuename)
  {
    try {
      return loadStrict(filename, keyname, valuename);
    }
    catch (SchemaException ex) {
      System.err.println("[KeyMap.load] WARNING - Loaded schema does not match expected schema: "+ex.getMessage());
    }
    catch (LoadingException ex) {
      System.err.println("[KeyMap.load] WARNING - Could not load "+filename+": "+ex.getMessage());
    }
    return new KeyMap(keyname, valuename);
  }

  public static KeyMap loadKeyMapFromTsv(String filepath, String colkey, String colvalue)
  {
    KeyMap ret = new KeyMap(colkey, colvalue);

    Input in_key = new InputFile(filepath, new String[] { colkey, colvalue});
    new Pusher("loadKeyMapFromTsv")
        .always(new OutputKeyMap(ret))
        .push(in_key);

    System.err.println("[KeyMap.loadFromFile] loaded: "+filepath+" -> "+ret.toString());
    return ret;
  }

  public void dumpTsv(PrintStream out)
  {
    Iterator<NativeHashLookup.Entry> it = map_.iterator();
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
      NativeHashLookup.Entry e = it.next();
      out.print(e.key);
      out.print('\t');
      out.print(e.value);
      out.println();
      ++ line_count;
    }
    System.err.println("DONE "+line_count);
  }

  public static void main(String[] args)
  {
    KeyMap bind;
    if ("-tsv".equals(args[0])) {
      bind = loadKeyMapFromTsv(args[1], args[2], args[3]);
      bind.save(args[4]);
      return;
    }
    else {
      try {
        bind = KeyMap.load(args[0]);
      }
      catch (LoadingException ex) {
        throw new RuntimeException(ex);
      }
    }

    if ("-value".equals(args[1])) {
      KeyMap ret = KeyMap.newLike(bind);
      Output out = new OutputKeyMap(ret);

      Pusher p = new Pusher();
      for (int i=2; i<args.length ; i++) {
        p.onlyIf(new IfEquals(bind.getColumnName(1), args[i]), out);
      }
      p.push(new InputKeyMap(bind));
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
      p.push(new InputKeyMap(bind));
    }
    else if ("-countall".equals(args[1])) {
      KeyMap ret = new KeyMap(bind.getColumnName(1), "count")
                             .countValues(bind);
      ret.dumpTsv(System.out);
    }
    else if ("-prompt".equals(args[1])) {
      try {
        jline.ConsoleReader reader = new jline.ConsoleReader();
        reader.setBellEnabled(false);
        reader.getHistory().setHistoryFile(new File(".KeyMap.history"));
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

