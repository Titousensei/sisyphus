package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class RowCounter
extends Modifier
{
  protected int row_;

  public RowCounter(String outcol)
  {
    super(new String[0], new String[] { outcol });
    row_ = 0;
  }

  public void compute(String[] input, String[] result)
  {
    result[0] = String.valueOf(row_);
    ++ row_;
  }

  @Override
  public String toStringModif()
  { return ".increment()"; }
}

