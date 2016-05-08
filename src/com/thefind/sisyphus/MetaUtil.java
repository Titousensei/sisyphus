package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class MetaUtil
{
  public final static String FILE_META_PREFIX = ".meta.";
  public final static String META_SCHEMA = "Schema:";

  public static String getMetaPath(String filename)
  {
    int cut = filename.lastIndexOf('/');
    if (cut==-1) {
      cut = filename.lastIndexOf(File.separatorChar);
    }
    if (cut==-1) {
      return FILE_META_PREFIX + filename;
    }
    cut++;
    return filename.substring(0, cut) + FILE_META_PREFIX + filename.substring(cut);
  }

  public static void saveMetaData(String filename, List<String> schema)
  {
    String metafile = getMetaPath(filename);
    try {
      PrintStream meta = new PrintStream(metafile, FileWriter.DEFAULT_CHARSET_STR);
      meta.print(META_SCHEMA);
      for (String col : schema) {
        meta.print('\t');
        meta.print(col);
      }
      meta.println();
      meta.close();
    }
    catch (UnsupportedEncodingException uuex) {
      System.err.println("[OutputFile.saveMeta] WARNING - Could not save metadata as utf-8: "+uuex.getMessage());
    }
    catch (FileNotFoundException fnfex) {
      System.err.println("[OutputFile.saveMeta] WARNING - Could not save metadata into: "+metafile);
    }
  }

  public static void saveAsText(Key key, String filename)
  { saveAsText(key, filename, EnumSet.noneOf(OutputFile.Flag.class)); }

  public static void saveAsText(Key key, String filename, EnumSet<OutputFile.Flag> flags)
  {
    Input in = new InputKey(key);
    saveAsText( in, filename, flags);
  }

  public static void saveAsText(KeyMap key, String filename)
  { saveAsText(key, filename, EnumSet.noneOf(OutputFile.Flag.class)); }

  public static void saveAsText(KeyMap key, String filename, EnumSet<OutputFile.Flag> flags)
  {
    Input in = new InputKeyMap(key);
    saveAsText( in, filename, flags);
  }

  private static void saveAsText(Input in, String filename, EnumSet<OutputFile.Flag> flags)
  {
    OutputFile out = new OutputFile(filename, flags, in.getSchemaOut());
    if(!flags.contains(OutputFile.Flag.NO_META)) {
      saveMetaData(filename, in.getSchemaOut());
    }
    out.open();
    in.open();
    String[] in_row = new String[in.getSchemaOut().size()];
    while (in.readRow(in_row)>-1) {
      out.append(in_row);
    }
    in.close();
    out.close();
  }
}
