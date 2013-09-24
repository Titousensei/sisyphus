package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;
import com.thefind.util.NativeHash;
import com.thefind.util.NativeHashSet;
import com.thefind.util.NativeHash.NativeHashIterator;
import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public abstract class Key
implements SchemaIn, SchemaOut, Serializable
{
  private static final long serialVersionUID = 1194293708528932487L;

  protected List<String> schema_;

  public Key(String... schema)
  { schema_ = CollectionUtil.asConstList(schema); }

  protected abstract NativeHash getNativeHash();

  public abstract boolean add(long key);

  public abstract boolean contains(long key);

  public abstract void remove(long key);

  public abstract void clear();

  public abstract NativeHashIterator keyIterator();

  public abstract int size();

  public abstract String toStringDump();

  public abstract String toStringStats();

  public abstract void dumpTsv(PrintStream out);

  public List<String> getSchemaIn() { return schema_; }

  public List<String> getSchemaOut() { return schema_; }

  @Override
  public String toString()
  { return getClass().getSimpleName()+"{"+schema_+", "+String.format("%,d", size())+" key"+(size()!=1?"s":"")+"}"; }

  /* UTILITIES */

  public String getColumnName(int idx)
  { return schema_.get(idx); }

  public Key filterExclude(Key rows_remove)
  {
    System.err.println("[Key.deleteAll] deleting: "+rows_remove);
    new Pusher()
        .always(new KeyDeleter(this))
        .push(new InputKey(rows_remove));
    return this;
  }

  public Key filterOnly(Key rows_keep)
  {
    Key rows_remove = new KeySet(schema_.get(0));
    Input in_rows   = new InputKey(this);

    new Pusher()
        .ifMiss(rows_keep, new OutputKey(rows_remove))
        .push(in_rows);

    int num_remove = rows_remove.size();
    if (num_remove>0) {
      new Pusher()
          .always(new KeyDeleter(this))
          .push(new InputKey(rows_remove));
    }

    return this;
  }

  /* KEY AS STRING */

  public boolean add(String key_str)
  {
    try {
      return add(Long.parseLong(key_str));
    } catch (NumberFormatException nfex) {
      return false;
    }
  }

  public boolean contains(String key_str)
  {
    try {
      return contains(Long.parseLong(key_str));
    } catch (NumberFormatException nfex) {
      return false ;
    }
  }

  public void remove(String key_str)
  {
    try {
      remove(Long.parseLong(key_str));
    } catch (NumberFormatException nfex) {
      return;
    }
  }

  /* SERIALIZATION */

  public KeySet asKeySet()
  {
    NativeHash nh = getNativeHash();
    NativeHashSet set = nh.asNativeHashSet();
    return new KeySet(getSchemaIn().get(0), set);
  }

  public void save(String filename)
  {
    System.err.println("[Key.save] saving: \""+filename+"\"");
    long t0 = System.currentTimeMillis();
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(
              new BufferedOutputStream(
                new FileOutputStream(filename)
              )
            );
      oos.writeObject(this);
      long t1 = System.currentTimeMillis();
      System.err.println("[Key.save] saved: "+toString()+" -> \""+filename+"\" in "
          + StringUtil.readableTime(t1-t0));
    }
    catch (IOException ioex) {
      ioex.printStackTrace();
    }
    finally {
      if (oos!=null) {
        try {
          oos.close();
        }
        catch (IOException ex) {}
      }
    }
  }

  // ois = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(fis)));
  // oos = new ObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(fos)));

  protected static <T> T load(Class<T> klass, String filename)
  throws LoadingException
  {
    System.err.println("[Key.load] loading: \""+filename+"\"");
    long t0 = System.currentTimeMillis();
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(
              new BufferedInputStream(
                new FileInputStream(filename)
              )
            );
      T ret = (T) ois.readObject();
      long t1 = System.currentTimeMillis();
      System.err.println("[Key.load] loaded: \""+filename+"\" -> "+ret.toString()
          +" in "+StringUtil.readableTime(t1-t0));

      return ret;
    }
    catch (FileNotFoundException ex) {
      throw new LoadingException("File not found: "+filename, ex);
    }
    catch (IOException ex) {
      throw new LoadingException("Bad Key file "+filename+": " + ex.toString(), ex);
    }
    catch (ClassCastException ex) {
      throw new LoadingException("Not a serialized "+klass.getSimpleName()+" file "+filename+": " + ex.getMessage(), ex);
    }
    catch (ClassNotFoundException ex) {
      throw new LoadingException("Unknown class "+klass.getSimpleName()+" in file "+filename+": " + ex.getMessage(), ex);
    }
    finally {
      if (ois!=null) {
        try {
          ois.close();
        }
        catch (IOException ex) {}
      }
    }
  }
/*
  public static Key loadStrict(String filename, String keyname)
  throws LoadingException, SchemaException
  {
    Key ret = load(Key.class, filename);
    List<String> sch = ret.getSchemaIn();
    if (keyname.equals(sch.get(0))) {
      return ret;
    }
    else {
      throw new SchemaException("["+keyname+","+valuename+"] != "+sch);
    }
  }

  public static Key load(String filename, String keyname)
  {
    try {
      return loadStrict(filename, keyname);
    }
    catch (LoadingException ex) {
      System.err.println("[Key.load] WARNING - Loading exception: "+ex.getMessage());
    }
    return new KeySet(keyname);
  }
*/
  public static Key loadFromFile(String colname, String filepath, String... schema)
  {
    Key ret = new KeySet(colname);

    Input in_key = new InputFile(filepath, schema);
    new Pusher()
        .always(new OutputKey(ret))
        .push(in_key);

    System.err.println("[Key.loadFromFile] loaded: "+filepath+" -> "+ret.toString());
    return ret;
  }

  public static Key loadFromFile(String colname, String filepath, String[] schema, Modifier... mod)
  {
    Key ret = new KeySet(colname);

    Input in_key = new InputFile(filepath, schema);
    Pusher p = new Pusher();
    for (Modifier m : mod) {
      p.always(m);
    }

    p.always(new OutputKey(ret))
     .push(in_key);

    System.err.println("[Key.loadFromFile] loaded: "+filepath+" -> "+ret.toString());
    return ret;
  }

  public static void main(String[] args)
  {
    Key key;
    try {
      key = Key.load(Key.class, args[0]);
    }
    catch (LoadingException ex) {
      throw new RuntimeException(ex);
    }

    System.out.print("#Schema:");
    for (String col : key.getSchemaOut()) {
      System.out.print('\t');
      System.out.print(col);
    }
    System.err.println();
    key.dumpTsv(System.out);
  }
}

