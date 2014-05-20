package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;
import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public class OutputStdout
extends Output
{
  public OutputStdout(String... schema)
  { this(CollectionUtil.asConstList(schema)); }

  public OutputStdout(List<String> schema)
  { super(schema); }

  @Override
  protected void append(String[] values)
  { System.out.println(StringUtil.join(values, "\t")); }

  @Override
  public boolean open()
  { return true; }

  @Override
  public void close()
  { }

  @Override
  public boolean sameAs(long that)
  { return false; }

  @Override
  public String toStringWhich()
  { return "STDOUT"; }
}

