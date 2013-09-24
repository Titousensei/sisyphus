package com.thefind.sisyphus;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author Eric Gaudet
 */
public class BinaryFileWriter
{
  protected final File file_;
  protected final boolean gzip_;

  protected OutputStream out_;

  public BinaryFileWriter(File file, boolean gzip)
  {
    file_ = file;
    gzip_ = gzip;

    out_ = null;
  }

  public void write(byte[] buffer, int offset, int length)
  {
    try {
      out_.write(buffer, offset, length);
    }
    catch (IOException ioex) {
      System.err.println("[BinaryFileWriter.write] ERROR - "+ioex.toString());
    }
  }

  public boolean isOpen()
  { return (out_!=null); }

  public boolean open()
  {
    if (out_!=null) {
      System.err.println("[FileWriter.open] ERROR - File already open: "+file_.getAbsolutePath());
      return false;
    }

    try {
      if (gzip_) {
         out_ = new BufferedOutputStream(
                    new GZIPOutputStream(
                        new FileOutputStream(file_, true)
                    ),
                    1024
                );
      }
      else {
         out_ = new BufferedOutputStream(
                    new FileOutputStream(file_, true),
                    1024
                );
      }
    }
    catch (FileNotFoundException fnfex) {
        System.err.println("[FileWriter.open] ERROR - File not found: "+file_.getAbsolutePath());
        return false;
    }
    catch (IOException ioex) {
      System.err.println("[FileWriter.open] ERROR - "+ioex.toString());
      return false;
    }

    return true;
  }

  public void close()
  {
    if (out_!=null) {
      try {
        out_.close();
      }
      catch (IOException ioex) {
        System.err.println("[FileWriter.close] ERROR - "+ioex.toString());
      }
    }
    out_ = null;
  }

  public void flush()
  {
    if (out_!=null) {
      try {
        out_.flush();
      }
      catch (IOException ioex) {
        System.err.println("[FileWriter.flush] ERROR - "+ioex.toString());
      }
    }
  }

  @Override
  public String toString()
  { return file_.getAbsolutePath(); }
}
