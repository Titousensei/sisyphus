package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class ColumnSubstring
extends Modifier
{
  protected final int begin_;
  protected final int end_;
  protected final String separator_;

  public ColumnSubstring(String outbefore, String outafter, String separator, String incol)
  {
    super(new String[] { incol }, new String[] { outbefore, outafter });
    separator_ = separator;
    begin_ = 0;
    end_   = 0;
  }

  public ColumnSubstring(String outcol, int begin, String incol)
  { this(outcol, begin, -1, incol); }

  public ColumnSubstring(String outcol, int begin, int end, String incol)
  {
    super(new String[] { incol }, new String[] { outcol });
    begin_ = begin;
    end_   = end;
    separator_ = null;
  }

  public void compute(String[] input, String[] result)
  {
    if (input[0] == null || input[0].isEmpty()) {
      for (int i=0 ; i<result.length ; i++) {
        result[i] = null;
      }
    }
    else if (separator_!=null) {
      String in = input[0];
      int cut = in.indexOf(separator_);
      if (cut==-1) {
        result[0] = null;
        result[1] = null;
      }
      else {
        result[0] = in.substring(0,cut);
        result[1] = in.substring(cut+separator_.length());
      }
    }
    else {
      String str = input[0];
      int l = str.length();
      int b = (begin_>=0) ? begin_ : l + begin_;
      int e = (end_>0) ? end_ : l + end_;
      result[0] = str.substring(b, e);
    }
  }

  @Override
  public String toStringModif()
  {
    return (end_==-1)
           ? ".substring("+begin_+")"
           : ".substring("+begin_+","+end_+")";
  }
}
