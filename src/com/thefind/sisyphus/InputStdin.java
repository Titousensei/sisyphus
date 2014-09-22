package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;
import com.thefind.util.StringUtil;

import static com.thefind.sisyphus.FileWriter.DEFAULT_CHARSET;

/**
 * @author Eric Gaudet
 */
public class InputStdin
extends Input
{
  protected final BufferedReader rd_;

  public InputStdin(String... schema)
  { this(CollectionUtil.asConstList(schema)); }

  public InputStdin(List<String> schema)
  {
    super(schema);
    rd_ = new BufferedReader(new InputStreamReader(System.in, DEFAULT_CHARSET));
  }

  @Override
  public boolean open()
  { return true; }

  @Override
  public void close()
  { }

  @Override
  protected int readRow(String[] result)
  {
    try {
      String line = rd_.readLine();
      if (line==null) {
        return -1;
      }

      return StringUtil.splitInto(line, '\t', result, 0);
    }
    catch (IOException ioex) {
      return -1;
    }
  }

  @Override
  public long getInternalHashCode()
  { return System.in.hashCode(); }

  @Override
  protected String toStringWhich()
  { return "STDIN"; }
}

