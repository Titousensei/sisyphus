package com.thefind.util;

import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.IOException;

import java.util.zip.GZIPInputStream;

/**
 * From workaround in: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4691425
 * When reading in the contents of a concatenated gzip file (one comprised of
 * more than one gzip file) GZIPInputStream returns EOF when it encounters the
 * first gzip trailer. When using GZIPInputStream on other platforms this
 * problem does not occur. The man page for gzip specifies that gunzip allows
 * for concatenated files.
 * Fixed in JDK7.
 *
 * @author Eric Gaudet
 */
public class GZIPInputStreamMulti
extends GZIPInputStream
{
  private final static int DEFAULT_SIZE = 1024;

  //private final GZIPInputStreamMulti parent_;
  private GZIPInputStreamMulti child_;

  private final int size_;
  private boolean eos_;

  public GZIPInputStreamMulti(InputStream in)
  throws IOException
  { this(in, DEFAULT_SIZE); }

  public GZIPInputStreamMulti(InputStream in, int size)
  throws IOException
  {
    super(new PushbackInputStream(in, size), size);
    size_ = size;
  }

  private GZIPInputStreamMulti(GZIPInputStreamMulti parent)
  throws IOException
  { this(parent, DEFAULT_SIZE); }

  private GZIPInputStreamMulti(GZIPInputStreamMulti parent, int size)
  throws IOException
  {
    super(parent.in, size);
    size_ = size;
    //parent_ = (parent.parent_==null) ? parent : parent.parent_;
    parent.child_ = this;
  }

  public int read(byte[] inputBuffer, int inputBufferOffset, int inputBufferLen)
  throws IOException
  {
    if (eos_) {
      return -1;
    }

    if (child_!=null) {
      return child_.read(inputBuffer, inputBufferOffset, inputBufferLen);
    }

    int charsRead=super.read(inputBuffer, inputBufferOffset, inputBufferLen);
    if (charsRead==-1)
    {
      // Push any remaining buffered data back onto the stream
      // If the stream is then not empty, use it to construct
      // a new instance of this class and delegate this and any
      // future calls to it...
      int n = inf.getRemaining() - 8;
      if (n > 0)
      {
        // More than 8 bytes remaining in deflater
        // First 8 are gzip trailer. Add the rest to
        // any un-read data...
        ((PushbackInputStream) this.in).unread(buf, len-n, n);
      }
      else
      {
        // Nothing in the buffer. We need to know whether or not
        // there is unread data available in the underlying stream
        // since the base class will not handle an empty file.
        // Read a byte to see if there is data and if so,
        // push it back onto the stream...
        byte[] b = new byte[1];
        int ret = this.in.read(b,0,1);
        if (ret==-1)
        {
          eos_ = true;
          return -1;
        }
        else {
          ((PushbackInputStream) this.in).unread(b, 0, 1);
        }
      }

      GZIPInputStreamMulti child = new GZIPInputStreamMulti(this, size_);

      return child.read(inputBuffer, inputBufferOffset, inputBufferLen);
    }
    else {
      return charsRead;
    }
  }
}

