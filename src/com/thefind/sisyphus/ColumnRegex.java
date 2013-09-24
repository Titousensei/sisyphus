package com.thefind.sisyphus;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eric Gaudet
 */
public class ColumnRegex
extends Modifier
{
  protected final Pattern regex_;
  protected int warnings_;

  public ColumnRegex(String incol, Pattern regex, String... outcols)
  {
    super(new String[] { incol }, outcols);
    regex_ = regex;
    warnings_ = 0;
  }

  public void compute(String[] input, String[] result)
  {
    Matcher m = regex_.matcher(input[0]);
    if (m.matches()) {
      for (int i=0 ; i<result.length ; i++) {
        result[i] = m.group(i+1);
      }
    }
    else {
      warnings_ ++;
    }
  }

  @Override
  public String toStringModif()
  { return ".match(\"" + regex_.pattern() + "\")"; }
}
