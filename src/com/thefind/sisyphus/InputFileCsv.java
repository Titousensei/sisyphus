package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import au.com.bytecode.opencsv.CSVParser;

/**
 * @since 0.3.0
 * @author Eric Gaudet
 */
public class InputFileCsv
extends InputFile
{
  protected CSVParser csv_parser_;

  public InputFileCsv(String filename)
  { super(filename); }

  public InputFileCsv(String filename, String... schema)
  { super(filename, schema); }

  public InputFileCsv(File file, String... schema)
  { super(file, schema); }

  public InputFileCsv(File file, List<String> schema)
  { super(file, schema); }

  @Override
  public boolean open()
  {
    boolean ret = super.open();
    if (ret) {
      csv_parser_ = new CSVParser();
    }
    return ret;
  }

  @Override
  protected int readRow(String[] result)
  {
    String line = rd_.readLine();
    if (line==null) {
      close();
      return -1;
    }

    result[0] = filename_;
    try {
      String[] columns = csv_parser_.parseLineMulti(line);
      int sz = result.length-1;
      if (columns.length<sz) {
        sz = columns.length;
      }
      System.arraycopy(columns, 0, result, 1, sz);
      return columns.length + 1;
    }
    catch (IOException ex) {
      // never happens with parseLineMulti
      return 1;
    }
  }
}

