package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.GZIPInputStreamMulti;
import com.thefind.util.StringUtil;

import static com.thefind.sisyphus.FileWriter.DEFAULT_CHARSET;

/**
 * @author Eric Gaudet
 */
public class FileReader
{
  protected final File file_;

  protected BufferedReader rd_;
  protected int lines_;

  public FileReader(String path)
  {
    file_ = new File(path);
    rd_ = null;
  }

  public FileReader(File file)
  {
    file_ = file;
    rd_ = null;
  }

  public boolean open()
  {
    if (rd_!=null) {
      System.err.println("[FileReader.open] WARNING - File already open: "+file_.getAbsolutePath());
      return true;
    }
    if (!file_.exists()) {
      System.err.println("[FileReader.open] ERROR - File not found: "+file_.getAbsolutePath());
      return false;
    }
    if (!file_.canRead()) {
      System.err.println("[FileReader.open] ERROR - File not readable: "+file_.getAbsolutePath());
      return false;
    }

    try {
      rd_ = new BufferedReader(
              new InputStreamReader(
                new GZIPInputStreamMulti(
                  new FileInputStream(file_)
                ), DEFAULT_CHARSET
              )
            );
    }
    catch (IOException ioex) {
      try {
        rd_ = new BufferedReader(
                new InputStreamReader(
                  new FileInputStream(file_),
                  DEFAULT_CHARSET
                )
              );
      }
      catch (IOException ioex2) {
        System.err.println("[FileReader.open] ERROR - "+ioex2.toString());
        return false;
      }
    }

    lines_ = 0;
    return true;
  }

  public boolean isOpen()
  { return (rd_!=null); }

  public int getLines()
  { return lines_; }

  public String getFilename()
  { return file_.getName(); }

  public String readLine()
  {
    try {
      lines_++;
      return rd_.readLine();
    }
    catch (IOException ioex) {
      ioex.printStackTrace();
      close();
    }

    return null;
  }

  Reader getReader()
  { return rd_; }

  public synchronized void close()
  {
    try {
      if (rd_!=null) {
        rd_.close();
      }
    }
    catch (IOException ioex) {
      System.err.println("[FileReader.closeFile] ERROR - "+ioex.toString());
    }
    finally {
      rd_ = null;
    }
  }

  public String toString()
  { return "\""+file_.getAbsolutePath()+"\" "+StringUtil.readableSize(file_.length()); }
}
