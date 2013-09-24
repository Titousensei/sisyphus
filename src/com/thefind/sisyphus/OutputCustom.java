package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * @author Eric Gaudet
 * @author Rajkumar Ponnusamy
 */
public abstract class OutputCustom
extends Output
{
  private final Output out_;

  protected OutputCustom(String[] in_schema, Output out, String[] out_schema)
  {
    super(in_schema);

    out_ = out;
    out_.ready(CollectionUtil.asConstList(out_schema));
  }

  @Override
  protected abstract void append(String[] row);

  protected abstract void flush();

  protected void writeRow(String... cols)
  { out_.use(cols); }

  @Override
  public boolean open()
  { return out_.open(); }

  @Override
  public void close()
  {
    flush();
    out_.close();
  }

  @Override
  public boolean sameAs(long that)
  { return out_.sameAs(that); }

  @Override
  protected String toStringWhich()
  { return out_.getSchemaIn().toString(); }

  @Override
  public String toString()
  {
    String ret = getClass().getSimpleName() + "{" + getSchemaIn()
           + " ~> " + toStringWhich()
           + " -> " + out_.toString()
           + " +" + String.format("%,d", getLines()) + " rows";
    if (warnings_>0) {
      ret += ", "+String.format("%,d", warnings_)+" warnings";
    }
    return ret+"}";
  }
}

