package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;
import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class OutputFile
extends Output
{
  public final static String FILE_META_PREFIX = ".meta.";
  public final static String META_SCHEMA = "Schema:";

  public final static String BACKUP_SUFFIX = "~";

  protected final String filename_;
  protected final String suffix_;
  protected final File file_;
  protected final EnumSet<Flag> flags_;
  protected FileWriter out_;

  // UNCOMPRESSED == APPEND
  public static enum Flag { CREATE_BACKUP, UNCOMPRESSED, ESCAPE_VALUES,
      KEEP_OPEN, SUFFIX_INCREMENT, SUFFIX_DATE, SUFFIX_TIMESTAMP, REPLACE, NO_META};

  public OutputFile(String filename, String... schema)
  { this(filename, EnumSet.noneOf(Flag.class), CollectionUtil.asConstList(schema)); }

  public OutputFile(String filename, Flag flag, String... schema)
  { this(filename, EnumSet.of(flag), CollectionUtil.asConstList(schema)); }

  public OutputFile(String filename, EnumSet<Flag> flags, String... schema)
  { this(filename, flags, CollectionUtil.asConstList(schema)); }

  public OutputFile(String filename, EnumSet<Flag> flags, List<String> schema)
  {
    super(schema);
    flags_ = flags;

    OutputFileHandler ofh = new OutputFileHandler(filename, flags);
    filename_ = ofh.filename;
    suffix_   = ofh.suffix;
    file_     = ofh.file;

    out_ = new FileWriter(file_, !flags_.contains(Flag.UNCOMPRESSED));
  }

  @Override
  protected void append(String[] values)
  {
    boolean tab = false;
    for (String v : values) {
      if (tab) {
        out_.print("\t");
      }
      if (v!=null) {
        if (flags_.contains(Flag.ESCAPE_VALUES)) {
          out_.print(StringUtil.escapeForTSV(v));
        }
        else {
          out_.print(v);
        }
      }
      tab = true;
    }
    out_.print("\n");
  }

  public File getFile()
  { return file_; }

  public boolean isOpen()
  { return out_.isOpen(); }

  public boolean openIO()
  { return out_.open(); }

  @Override
  public boolean open()
  {
    if (flags_.contains(Flag.KEEP_OPEN) && isOpen()) {
      return true;
    }

    File curfile = new File(filename_);
    if (curfile.exists()) {
      if (flags_.contains(Flag.REPLACE)) {
        if (curfile.delete()) {
          if (verbose_) { System.err.println("[OutputSplit] Flag.REPLACE: deleted file "+curfile.getAbsolutePath()); }
        }
        else {
          System.err.println("[OutputSplit] WARNING - Flag.REPLACE: Failed to delete file "+curfile.getAbsolutePath());
        }
      }
      else if (!flags_.contains(Flag.UNCOMPRESSED)) {
        throw new PusherException("OutputFile already exists: "+curfile.getAbsolutePath()+", "+toString());
      }
    }

    if (openIO()) {
      if(!flags_.contains(Flag.NO_META)) {
       saveMetaData();
      }
      return true;
    }

    return false;
  }

  @Override
  public void close()
  {
    if (flags_.contains(Flag.KEEP_OPEN)) {
      out_.flush();
    }
    else {
      closeFile();
    }
  }

  public void closeFile()
  { out_.close(); }

  @Override
  public boolean sameAs(long that)
  { return (filename_!=null) && (that==filename_.hashCode()); }

  @Override
  protected String toStringWhich()
  { return out_.toString(); }

  public String getSuffix()
  { return suffix_; }

  /*UTILITY EXPORT METHODS*/

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

  protected void saveMetaData()
  {
    String metafile = OutputFile.getMetaPath(filename_);
    try {
      PrintStream meta = new PrintStream(metafile, FileWriter.DEFAULT_CHARSET_STR);
      meta.print(META_SCHEMA);
      for (String col : getSchemaIn()) {
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
  { saveAsText(key, filename, EnumSet.noneOf(Flag.class)); }

  public static void saveAsText(Key key, String filename, EnumSet<Flag> flags)
  {
    Input in = new InputKey(key);
    saveAsText( in, filename, flags);
  }

  public static void saveAsText(KeyMap key, String filename)
  { saveAsText(key, filename, EnumSet.noneOf(Flag.class)); }

  public static void saveAsText(KeyMap key, String filename, EnumSet<Flag> flags)
  {
    Input in = new InputKeyMap(key);
    saveAsText( in, filename, flags);
  }

  private static void saveAsText(Input in, String filename, EnumSet<Flag> flags)
  {
    OutputFile out = new OutputFile(filename, flags, in.getSchemaOut().toArray(new String[0]));
    if(!flags.contains(Flag.NO_META)) {
      out.saveMetaData();
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

  /*FILE CREATION AND FLAGS HANDLING*/

  protected static class OutputFileHandler
  {
    public final String filename;
    public final String suffix;
    public final File   file;

    public OutputFileHandler(String fileprefix, EnumSet<Flag> flags)
    throws IllegalArgumentException
    {
      if (flags.contains(Flag.SUFFIX_TIMESTAMP)) {
        if (flags.contains(Flag.SUFFIX_DATE))      { throw new IllegalArgumentException("OutputFile Flags can't set both SUFFIX_TIMESTAMP and SUFFIX_DATE"); }
        if (flags.contains(Flag.SUFFIX_INCREMENT)) { throw new IllegalArgumentException("OutputFile Flags can't set both SUFFIX_TIMESTAMP and SUFFIX_INCREMENT"); }
        if (flags.contains(Flag.CREATE_BACKUP))    { throw new IllegalArgumentException("OutputFile Flags can't set both SUFFIX_TIMESTAMP and CREATE_BACKUP"); }
        suffix = StringUtil.SIMPLE_TIMESTAMP_SUFFIX.get().format(new Date());
        filename = fileprefix + "." + suffix;
      }
      else if (flags.contains(Flag.SUFFIX_DATE)) {
        if (flags.contains(Flag.SUFFIX_INCREMENT)) { throw new IllegalArgumentException("OutputFile Flags can't set both SUFFIX_DATE and SUFFIX_INCREMENT"); }
        suffix = StringUtil.SIMPLE_DATE.get().format(new Date());
        filename = fileprefix + "." + suffix;
      }
      else if (flags.contains(Flag.SUFFIX_INCREMENT)) {
        if (flags.contains(Flag.CREATE_BACKUP))    { throw new IllegalArgumentException("OutputFile Flags can't set both SUFFIX_DATE and CREATE_BACKUP"); }
        int s = 0;
        while (true) {
          String f_str = fileprefix + "." + s;
          File f = new File(f_str);
          if (f.exists()) {
            ++ s;
          }
          else {
            filename = f_str;
            suffix = String.valueOf(s);
            break;
          }
        }
      }
      else {
        filename = fileprefix;
        suffix = "";
      }

      if (flags.contains(Flag.UNCOMPRESSED) && flags.contains(Flag.KEEP_OPEN)) {
        System.err.println("[OutputFile] WARNING - KEEP_OPEN unnecessary for UNCOMPRESSED files");
      }

      file = new File(filename);
      if (flags.contains(Flag.CREATE_BACKUP)) {
        File bakfile = new File(filename + BACKUP_SUFFIX);
        if (bakfile.exists()) {
          if (bakfile.delete()) {
            System.err.println("[OutputSplit] INFO - Deleted backup file: "+bakfile.getAbsolutePath());
          }
          else {
            System.err.println("[OutputSplit] WARNING - Failed to delete backup file: "+bakfile.getAbsolutePath());
          }
        }
        if (file.exists()) {
          file.renameTo(bakfile);
          if (!file.renameTo(bakfile)) {
            System.err.println("[OutputSplit] WARNING - Failed to rename to backup file: "+file.getAbsolutePath());
          }
        }
      }
    }
  }
}
