package com.thefind.sisyphus;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eric Gaudet
 */
public class ColumnReplaceAll
extends Modifier
{
  protected final Pattern regex_;
  protected final String repl_;

  public ColumnReplaceAll(String incol, Pattern regex, String repl, String outcol)
  {
    super(new String[] { incol }, new String[] { outcol });
    regex_ = regex;
    repl_  = repl;
  }

  public void compute(String[] input, String[] result)
  {
    Matcher m = regex_.matcher(input[0]);
    result[0] = m.replaceAll(repl_);
  }

  @Override
  public String toStringModif()
  { return ".replaceAll(\"" + regex_.pattern() + "\", \"" + repl_ +"\")"; }
}
