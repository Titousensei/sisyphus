package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 * @author Qin Wang
 */
public class ColumnsJoin
extends Modifier
{
  private final String join_;

  public ColumnsJoin(String outcol, String join, String... incol)
  {
    super(incol, new String[] { outcol });
    join_ = join;
  }

  public void compute(String[] input, String[] result)
  { result[0] = StringUtil.join(input, join_); }

  @Override
  public String toStringModif()
  { return ".join('"+join_+"')"; }
}
