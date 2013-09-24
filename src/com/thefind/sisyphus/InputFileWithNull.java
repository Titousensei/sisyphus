package com.thefind.sisyphus;

import java.io.*;

import com.thefind.util.CollectionUtil;

/**
 * @author Eric Gaudet
 */
public class InputFileWithNull
extends InputFile
{
  public InputFileWithNull(String filename, String... schema)
  { super(new File(filename), CollectionUtil.asConstList(schema)); }

  @Override
  protected int readRow(String[] result)
  {
    int j = super.readRow(result);
    if (j>0) {
      for (int i=0 ; i<j ; i++) {
        if ("NULL".equalsIgnoreCase(result[i])) {
            result[i] = null;
        }
      }
    }
    return j;
  }
}

