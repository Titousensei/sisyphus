package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public class SplitByOneColumn
extends OutputFileSplit
{
  protected final String basedir_;
  protected final String prefix_;
  protected final String suffix_;

  protected final String col_split_;

  public SplitByOneColumn(String basedir, String prefix, String col_split, String suffix,
      String... schema_out)
  {
    super(new String[] { col_split }, schema_out);
    basedir_ = basedir;
    prefix_  = prefix;
    suffix_  = suffix;
    col_split_ = col_split;
  }

  public String getFilename(String[] format)
  {
    String ret = getCache(format[0]);
    if (ret==null) {
      ret = basedir_+"/"+prefix_+format[0]+suffix_;
      putCache(format[0], ret);
      if (verbose_) {
        System.err.println("[SplitByOneColumn] new filename: ["+StringUtil.join(format, ", ")+"] -> \""+ret+"\"");
      }
    }
    return ret;
  }

  public String getSampleFilename()
  { return basedir_+"/"+prefix_+"<"+col_split_+">"+suffix_; }
}
