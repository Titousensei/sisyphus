package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class ColumnsFormat
extends Modifier
{
  private final String format_;

  public ColumnsFormat(String outcol, String format, String... incol)
  {
    super(incol, new String[] { outcol });
    format_ = format;
  }

  public void compute(String[] input, String[] result)
  { result[0] = String.format(format_, (Object[]) input); }

  @Override
  public String toStringModif()
  { return ".format('"+format_+"')"; }
}
