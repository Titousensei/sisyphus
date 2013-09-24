package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;
import com.thefind.util.StringUtil;

import static com.thefind.sisyphus.InputFile.*;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class InputFileGroup
extends Input
{
  protected final int schema_size_;
  protected final FileGroup group_;

  protected int file_num_;
  protected FileReader rd_;
  protected String filename_;

  public InputFileGroup(String dirname, String filter, String... schema)
  {
    super(getFullSchema(schema));
    group_ = new FileGroup(dirname, filter);
    schema_size_ = schema.length;

    rd_ = null;
    filename_ = null;
  }

  private static List<String> getFullSchema(String... schema)
  {
    List<String> ret = new ArrayList(schema.length+1);
    ret.add(COL_FILENAME);
    for (int i=0 ; i<schema.length ; i++) {
      ret.add(schema[i]);
    }
    return CollectionUtil.asConstList(ret);
  }

  @Override
  public boolean open()
  {
    file_num_ = 0;
    if (group_.files_.length==0) return false;
    File f = group_.files_[file_num_];
    filename_ = f.getName();
    rd_ = new FileReader(f);
    System.err.println("[InputFileGroup] ... starting: "+filename_
        +" - "+(file_num_+1)+"/"+group_.files_.length);
    return (rd_.open());
  }

  //public String getFilename()
  //{ return group_.files_[file_num_].getName(); }

  @Override
  public void close()
  {
    if (rd_!=null) {
      System.err.println("[InputFileGroup] ... finished: "+filename_
            +" - "+String.format("%,d", rd_.getLines())+" rows");
      rd_.close();
      rd_ = null;
    }
  }

  @Override
  protected int readRow(String[] result)
  {
    String line = rd_.readLine();
    if (line==null) {
      close();
      file_num_++;
      if (file_num_<group_.files_.length) {
        File f = group_.files_[file_num_];
        filename_ = f.getName();
        rd_ = new FileReader(f);
        System.err.println("[InputFileGroup] ... starting: "+filename_
            +" - "+(file_num_+1)+"/"+group_.files_.length);
        if (rd_.open()) {
          return readRow(result);
        }
      }
      return -1;
    }

    int ret = StringUtil.splitInto(line, '\t', result, 1);
    result[0] = filename_;
    return ret;
  }

  @Override
  protected long getInternalHashCode()
  { return group_.str_.hashCode(); }

  @Override
  public boolean sameAs(Action act)
  { return group_.sameAs(act); }

  @Override
  protected String toStringWhich()
  { return group_.toString(); }

  public File[] getInputFiles() {
    return group_.files_;
  }
}

