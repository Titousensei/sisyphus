package com.thefind.sisyphus;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author Eric Gaudet
 */
public class FileWriter
{
  public final static String  DEFAULT_CHARSET_STR = "UTF-8";
  public final static Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_STR);

  public final static String STDOUT = "STDOUT";

  protected final File file_;
  protected final boolean gzip_;

  protected Writer out_;

  public FileWriter(File file, boolean gzip)
  {
    file_ = file;
    gzip_ = gzip;

    out_ = null;
  }

  public void print(String str)
  {
    try {
      out_.write(str);
    }
    catch (IOException ioex) {
      System.err.println("[FileWriter.print] ERROR - "+ioex.toString());
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

    if (file_!=null) {
      try {
        if (gzip_) {
           out_ = new BufferedWriter(
                    new OutputStreamWriter(
                      new GZIPOutputStream(
                        new FileOutputStream(file_, true)
                      ), // { { def.setLevel(java.util.zip.Deflater.BEST_COMPRESSION); } },
                    DEFAULT_CHARSET_STR),
                    1024);
           /*
              compressed: speed  size1  size2
              9  (2835K rpm)  58,613,789  35,960,759
              8  (2909K rpm)  58,613,786  35,968,880
              7  (3928K rpm)  58,719,374  36,570,072
              6  (4112K rpm)  58,803,169  37,451,769  *DEFAULT*
              5  (4373K rpm)  59,068,556  38,081,595
              4  (4924K rpm)  60,013,510  39,774,558
              3  (5069K rpm)  60,716,650  40,942,332
              2  (5274K rpm)  62,623,283  42,589,495
              1  (5437K rpm)  63,039,395  44,294,660
              uncompressed:
              0  (6275K rpm) 138,170,705 158,164,269
           */
        }
        else {
           out_ = new BufferedWriter(
                    new OutputStreamWriter(
                      new FileOutputStream(file_, true),
                      DEFAULT_CHARSET_STR),
                    1024);
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
    }
    else {
      try {
        out_ = new BufferedWriter(
                 new OutputStreamWriter(
                   new FileOutputStream(java.io.FileDescriptor.out),
                   DEFAULT_CHARSET_STR),
                 1024);
      }
      catch (IOException ioex) {
        System.err.println("[FileWriter.open] ERROR STDOUT - "+ioex.toString());
        return false;
      }
    }

    return true;
  }

  public void close()
  {
    if (file_!=null && out_!=null) {
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
    if (file_!=null && out_!=null) {
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
  { return (file_!=null) ? "\""+file_.getAbsolutePath()+"\"" : STDOUT; }

  /**
   * Perfomance test
   * Was: PrintStream(BufferedOutputStream())
   *    file time: 1244
   *    gzip time: 1843
   * Now: BufferedWriter(new OutputStreamWriter(), 1024)
   *    file time: 355
   *    gzip time: 1022
   */
  public static void main(String... args)
  {
    FileWriter out;
    long start;

    /*
    out = new FileWriter(null, false);
    out.open();
    start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      out.print("abcdefghijk ");
      out.print(String.valueOf(i));
      out.print("\n");
    }
    out.flush();
    out.close();
    System.err.println("STDOUT time: " + (System.currentTimeMillis() - start));
    */

    out = new FileWriter(new File("test.out"), false);
    out.open();
    start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      out.print("abcdefghijk ");
      out.print(String.valueOf(i));
      out.print("\n");
    }
    out.flush();
    out.close();
    System.err.println("file time: " + (System.currentTimeMillis() - start));

    out = new FileWriter(new File("test.out.gz"), true);
    out.open();
    start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      out.print("abcdefghijk ");
      out.print(String.valueOf(i));
      out.print("\n");
    }
    out.flush();
    out.close();
    System.err.println("gzip time: " + (System.currentTimeMillis() - start));
  }
}

