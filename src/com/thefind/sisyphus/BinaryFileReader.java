package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.GZIPInputStreamMulti;
import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 */
public class BinaryFileReader
{
  protected final File file_;

  protected BufferedInputStream ris_;
  protected long rows_;

  public BinaryFileReader(String path)
  { this(new File(path)); }

  public BinaryFileReader(File file)
  {
    file_ = file;
    ris_ = null;
  }

  public boolean open()
  {
    if (ris_!=null) {
      System.err.println("[BinaryFileReader.open] WARNING - File already open: "+file_.getAbsolutePath());
      return true;
    }
    if (!file_.exists()) {
      System.err.println("[BinaryFileReader.open] ERROR - File not found: "+file_.getAbsolutePath());
      return false;
    }
    if (!file_.canRead()) {
      System.err.println("[BinaryFileReader.open] ERROR - File not readable: "+file_.getAbsolutePath());
      return false;
    }

    try {
      ris_ = new BufferedInputStream(
                new GZIPInputStreamMulti(
                  new FileInputStream(file_)
                )
            );
    }
    catch (IOException ioex) {
      try {
        ris_ = new BufferedInputStream(
                  new FileInputStream(file_)
              );
      }
      catch (IOException ioex2) {
        System.err.println("[BinaryFileReader.open] ERROR - "+ioex2.toString());
        return false;
      }
    }

    rows_ = 0L;
    return true;
  }

  public boolean isOpen()
  { return (ris_!=null); }

  public long getLines()
  { return rows_; }

  public String getFilename()
  { return file_.getName(); }

  public int read(byte[] buffer, int off, int len)
  {
    try {
      rows_++;
       return ris_.read(buffer, off, len);
    }
    catch (IOException ioex) {
      ioex.printStackTrace();
      close();
    }
    return -1;
  }

  public synchronized void close()
  {
    try {
      if (ris_!=null) {
        ris_.close();
      }
    }
    catch (IOException ioex) {
      System.err.println("[BinaryFileReader.closeFile] ERROR - "+ioex.toString());
    }
    finally {
      ris_ = null;
    }
  }

  public String toString()
  { return "\""+file_.getAbsolutePath()+"\" "+StringUtil.readableSize(file_.length()); }
}

