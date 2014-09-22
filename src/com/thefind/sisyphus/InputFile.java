package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;
import com.thefind.util.IOUtil;
import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public class InputFile
extends Input
{
  public final static String COL_FILENAME = "__filename__";

  protected final int schema_size_;
  protected final String filename_;

  protected final FileReader rd_;

  protected String header_until_ = null;
  protected int header_lines_ = 0;

  public InputFile(String filename)
  { this(new File(filename), InputFile.readMetaSchema(filename, true)); }

  public InputFile(String filename, String... schema)
  { this(new File(filename), CollectionUtil.asConstList(schema)); }

  public InputFile(File file, String... schema)
  { this(file, CollectionUtil.asConstList(schema)); }

  public InputFile(File file, List<String> schema)
  {
    super(getFullSchema(schema));
    filename_ = file.getName();
    schema_size_ = schema.size();

    rd_ = new FileReader(file);

    List<String> metaschema = readMetaSchema(filename_, false);
    if (metaschema!=null) {
      for (int i=0 ; i<schema_size_ ; i++ ) {
        if (!schema.get(i).equals(metaschema.get(i))) {
          throw new SchemaException("Expected schema "+schema
              +" does not match saved metadata "+metaschema);
        }
      }
    }
  }

  private static List<String> getFullSchema(List<String> schema)
  {
    List<String> ret = new ArrayList();
    ret.add(COL_FILENAME);
    ret.addAll(schema);
    return CollectionUtil.asConstList(ret);
  }

  private static List<String> readMetaSchema(String filename, boolean exceptions)
  {
    String metafile = OutputFile.FILE_META_PREFIX+filename;
    try {
      List<String> in = IOUtil.readFileLines(metafile);
      for (String line : in) {
        if (line.startsWith(OutputFile.META_SCHEMA)) {
          String[] schema = line.substring(OutputFile.META_SCHEMA.length()+1).split("\t");
          return CollectionUtil.asConstList(schema);
        }
      }
    }
    catch (FileNotFoundException fnfex) {
      if (exceptions) {
        throw new SchemaException(fnfex.toString());
      }
      else {
        return null;
      }
    }
    catch (IOException ioex) {
      ioex.printStackTrace();
    }

    throw new SchemaException("No row \""+OutputFile.META_SCHEMA+"\" in "+metafile);
  }

  @Override
  public boolean open()
  {
    if (rd_.open()) {
      if (header_until_!=null) {
        System.err.println("[InputFile.open] skipping header lines until \""+header_until_+"\"");
        while (true) {
          String line = rd_.readLine();
          if (line==null) {
            System.err.println("[InputFile.open] ... EOF reached");
            return false;
          }
          else if (line.startsWith(header_until_)) {
            System.err.println("[InputFile.open] ... found header line \""+line+"\"");
            return true;
          }
        }
      }

      if (header_lines_>0) {
        System.err.println("[InputFile.open] skiping "+header_lines_+" header lines");
        for (int i=0 ; i<header_lines_ ; i++) {
          String line = rd_.readLine();
          if (line==null) {
            System.err.println("[InputFile.open] ... EOF reached");
            return false;
          }
        }
      }

      return true;
    }

    return false;
  }

  public InputFile skipHeader(String after)
  {
    header_until_ = after;
    return this;
  }

  public InputFile skipHeader(int num_lines)
  {
    header_lines_ = num_lines;
    return this;
  }

  @Override
  public void close()
  { rd_.close(); }

  @Override
  protected int readRow(String[] result)
  {
    String line = rd_.readLine();
    if (line==null) {
      close();
      return -1;
    }

    //int ret = StringUtil.splitInto(line, PATTERN_TSV, result, 1);
    int ret = StringUtil.splitInto(line, '\t', result, 1);
    result[0] = filename_;
    return ret;
  }

  @Override
  public long getInternalHashCode()
  { return filename_.hashCode(); }

  @Override
  protected String toStringWhich()
  { return rd_.toString(); }

  // don't forget the handle the .meta files
  public static Key loadKey(String filename)
  { throw new RuntimeException("IMPLEMENT ME!!!"); }

  public static Key loadKey(String filename, int col_key, String schema_key)
  { throw new RuntimeException("IMPLEMENT ME!!!"); }

  // don't forget the handle the .meta files
  public static KeyMap loadKeyMap(String filename)
  { throw new RuntimeException("IMPLEMENT ME!!!"); }

  // don't forget the handle the .meta files
  public static KeyMap loadKeyMap(String filename, int col_key, int col_value, String schema_key, String schema_value)
  { throw new RuntimeException("IMPLEMENT ME!!!"); }
}

