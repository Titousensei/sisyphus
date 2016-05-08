package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * A class to save data into fixed-sized records binary file
 * Sizes are the number of bytes for each field in the record (row)
 * Use a negative value for sizes if the column is in hexadecimal representation.
 * Fields smaller or equal than 8 bytes (long) will assume decimal representation,
 * unless their specified size is negative.
 *
 * <pre>
 * Example:
 *   size  |  representation          size  |  representation
 *     1   |  decimal                  -1   |  hexadecimal
 *     2   |  decimal                  -2   |  hexadecimal
 *     3   |  decimal                  -3   |  hexadecimal
 *     4   |  decimal                  -4   |  hexadecimal
 *     5   |  decimal                  -5   |  hexadecimal
 *     6   |  decimal                  -6   |  hexadecimal
 *     7   |  decimal                  -7   |  hexadecimal
 *     8   |  decimal                  -8   |  hexadecimal
 *     9   |  hexadecimal              -9   |  hexadecimal
 *    16   |  hexadecimal             -16   |  hexadecimal
 *  1000   |  hexadecimal           -1000   |  hexadecimal
 * </pre>
 *
 * @author Eric Gaudet
 */
public class OutputBinaryFile
extends OutputFile
{
  public final static String META_BINSIZE = "BinarySizes:";

  protected final int columns_total_;
  protected final int[] columns_sizes_;
  protected final byte[] buffer_;
  protected BinaryFileWriter bin_;

  public OutputBinaryFile(String filename, int[] sz, String... schema)
  { this(filename, EnumSet.noneOf(Flag.class), sz, CollectionUtil.asConstList(schema)); }

  public OutputBinaryFile(String filename, Flag flag, int[] sz, String... schema)
  { this(filename, EnumSet.of(flag), sz, CollectionUtil.asConstList(schema)); }

  public OutputBinaryFile(String filename, EnumSet<Flag> flags, int[] sz, String... schema)
  { this(filename, flags, sz, CollectionUtil.asConstList(schema)); }

  public OutputBinaryFile(String filename, EnumSet<Flag> flags, int[] sz, List<String> schema)
  {
    super(filename, flags, schema);

    columns_sizes_ = Arrays.copyOf(sz, sz.length);
    columns_total_ = InputBinaryFile.verifyColumnSizes(columns_sizes_, schema);
    buffer_ = new byte[columns_total_];

    bin_ = new BinaryFileWriter(file_, !flags_.contains(Flag.UNCOMPRESSED));
  }

  @Override
  protected void append(String[] values)
  {
    boolean hasWarning = false;
    int b = 0;
    for (int j=0 ; j<schema_.size() ; ++ j ) {
      String val = values[j];
      int sz = columns_sizes_[j];
      if (0<=sz && sz<=8) {
        long num = Long.parseLong(val);
        b += sz;
        int b0 = b;
        for (int i=0 ; i<sz ; ++ i) {
          -- b0;
          buffer_[b0] = (byte) (num & 0xFF);
          num >>>= 8L;
        }
      }
      else {
        sz = Math.abs(sz);
        if (val.length()!=sz*2) {
          hasWarning = true;
          if (val.length()<sz*2) {
            sz = val.length()/2;
          }
        }
        for (int i=0 ; i<sz ; ++ i) {
          try {
            String hex = val.substring(i*2, (i+1)*2);
            int num = Integer.parseInt(hex,16);
            buffer_[b] = (byte) (num & 0xFF);
          }
          catch (NumberFormatException ex) {
            buffer_[b] = 0;
            hasWarning = true;
          }
          ++ b;
        }
      }
    }
    bin_.write(buffer_, 0, columns_total_);
    //System.out.println(">>> "+getLines()+" "+Arrays.toString(buffer_));
    if (hasWarning) { ++ warnings_; }
  }


  @Override
  public boolean isOpen()
  { return bin_.isOpen(); }

  @Override
  public boolean openIO()
  { return bin_.open(); }

  @Override
  public void close()
  {
    if (flags_.contains(Flag.KEEP_OPEN)) {
      bin_.flush();
    }
    else {
      closeFile();
    }
  }

  @Override
  public void closeFile()
  { bin_.close(); }

  @Override
  protected String toStringWhich()
  { return "bin:"+bin_.toString(); }

  /*UTILITY EXPORT METHODS*/

  private static void saveAsBinary(Input in, String filename, EnumSet<Flag> flags, int[] sz)
  {
    OutputFile out = new OutputBinaryFile(filename, flags, sz, in.getSchemaOut());
    if (!flags.contains(Flag.NO_META)) {
      MetaUtil.saveMetaData(filename, in.getSchemaOut());
    }
    out.open();
    in.open();
    String[] in_row = new String[in.getSchemaOut().size()];
    while (in.readRow(in_row)>-1) {
      out.append(in_row);
    }
    in.close();
    out.close();
  }

  public static void main(String[] args)
  {
    String[] SCHEMA = new String[] { "urlhash", "siteid", "eedbid", "skiprow" };
    int[] SIZES = new int[] { 8, 4, 4, 16 };
    Input in = new InputFile(args[0], SCHEMA);
    Output out = new OutputBinaryFile(args[1],
        EnumSet.of(OutputFile.Flag.REPLACE, OutputFile.Flag.UNCOMPRESSED),
        SIZES,
        SCHEMA);

    new Pusher().debug()
      .always(out)
      .push(in);
  }
}

