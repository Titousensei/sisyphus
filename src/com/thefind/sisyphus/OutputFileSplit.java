package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;
import com.thefind.util.LruMap;

/**
 * @author Eric Gaudet
 */
public abstract class OutputFileSplit
extends Output
{
  private final Map<String, String> name_cache_;
  private Map<String, OutputFile> out_cache_;

  protected final SchemaAdapter adapter_split_;
  protected final SchemaAdapter adapter_out_;

  private String current_filename_;
  private OutputFile out_;

  private KeepOpen keep_open_;
  private boolean compressed_;

  private int switch_count_;

  /**
   * levels of keep open:
   * - NO: close the previous file at each switch
   * - ON_SWITCH: keep the files open on switch, but close them all at the end
   * - YES: keep all the files open until explicitly closed by closeFiles()
   * - LRU: keep the [default 100] last accessed files open
   */
  public static enum KeepOpen { NO, ON_SWITCH, YES, LRU };

  protected OutputFileSplit(String[] schema_split, String[] schema_out)
  {
    super(CollectionUtil.merge(schema_split, schema_out));
    adapter_split_ = new SchemaAdapter(schema_split);
    adapter_out_   = new SchemaAdapter(schema_out);

    current_filename_ = null;
    out_ = null;

    name_cache_   = new HashMap();
    out_cache_    = new HashMap();
    switch_count_ = 0;
    keep_open_    = KeepOpen.NO;
    compressed_   = false;
  }

  protected abstract String getFilename(String[] format);

  protected abstract String getSampleFilename();

  protected String getCache(String key)
  { return name_cache_.get(key); }

  protected void putCache(String key, String val)
  { name_cache_.put(key, val); }

  public void setKeepOpen(KeepOpen ko)
  {
    if (ko == KeepOpen.LRU) {
      setKeepOpenLru(100);
    }
    else {
      keep_open_ = ko;
      if (verbose_) {
        System.err.println("[OutputFileSplit] setKeepOpen \""+getSampleFilename()+"\" "+keep_open_);
      }
    }
  }

  public void setKeepOpenLru(int maxfiles)
  {
    keep_open_ = KeepOpen.LRU;
    out_cache_ = new LruFiles(maxfiles);
    if (verbose_) {
      System.err.println("[OutputFileSplit] setKeepOpen \""+getSampleFilename()+"\" LRU "+maxfiles);
    }
  }

  public void setCompressed(boolean compressed)
  {
    compressed_ = compressed;
    if (verbose_) {
      System.err.println("[OutputFileSplit] setCompressed \""+getSampleFilename()+"\" "+compressed_);
    }
  }

  @Override
  protected void readySchema(List<String> schema_row)
  {
    adapter_split_.setSchemaIn(schema_row);
    adapter_out_.setSchemaIn(schema_row);
  }

  @Override
  protected void append(String[] row)
  {
    String[] format = adapter_split_.getView(row);
    String filename = getFilename(format);

    if (keep_open_!=KeepOpen.NO) {
      out_ = out_cache_.get(filename);
    }
    else if (current_filename_==null) {
      current_filename_ = filename;
    }
    else if (!current_filename_.equals(filename)) {
      current_filename_ = filename;
      out_.close();
      out_ = null;
      switch_count_ ++;
    }

    if (out_==null) {
      out_ = new OutputFile(filename,
                 compressed_
                   ? EnumSet.noneOf(OutputFile.Flag.class)
                   : EnumSet.of(OutputFile.Flag.UNCOMPRESSED),
                 schema_);
      out_.open();
      if (keep_open_!=KeepOpen.NO) {
        out_cache_.put(filename, out_);
      }
    }

    String[] values = adapter_out_.getView(row);
    out_.append(values);
  }

  @Override
  public boolean open()
  { return true; }

  @Override
  public void close()
  {
    if (keep_open_==KeepOpen.ON_SWITCH) {
      closeFiles();
    }
    else if (keep_open_==KeepOpen.NO) {
      if (out_ != null) {
        out_.close();
        out_ = null;
      }
    }
  }

  public void closeFiles()
  {
    if (keep_open_!=KeepOpen.NO) {
      for (OutputFile out : out_cache_.values()) {
        out.close();
      }
      out_cache_.clear();
    }
  }

  @Override
  public boolean sameAs(long that)
  { return false; }

  @Override
  protected String toStringWhich()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(adapter_out_.getSchemaOut())
      .append(" \"")
      .append(getSampleFilename())
      .append("\" (")
      .append(name_cache_.size())
      .append(" files, ");
    switch (keep_open_) {
    case NO:
      sb.append(switch_count_)
        .append(" switched");
      break;
    case ON_SWITCH:
      sb.append("keep_open");
      break;
    case YES:
      sb.append("keep_open_on_switch");
      break;
    case LRU:
      sb.append("lru=")
        .append(((LruMap) out_cache_).capacity());
      break;
    }
    sb.append(')');
    return sb.toString();
  }

  private static class LruFiles
  extends LruMap<String, OutputFile>
  {
    private LruFiles(int capacity)
    { super(capacity); }

    @Override
    protected void onRemove(String key, OutputFile value)
    { value.close(); }
  }
}

