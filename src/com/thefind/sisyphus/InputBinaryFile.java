package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * A class to load data from fixed-sized records binary file
 * Sizes are the number of bytes for each field in the record (row)
 * Use a negative value for sizes to force the column into hexadecimal representation.
 * Fields smaller or equal than 8 bytes (long) will be transformed into decimal,
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
public class InputBinaryFile
extends Input
{
  public final static String COL_FILENAME = "__filename__";
  public final static char[] HEX_DIGIT = new char[] {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  protected final int schema_size_;
  protected final int columns_total_;
  protected final int[] columns_sizes_;
  protected final String filename_;
  protected final byte[] buffer_;

  protected final BinaryFileReader rd_;

  public InputBinaryFile(String filename, int[] sz, String... schema)
  { this(new File(filename), sz, CollectionUtil.asConstList(schema)); }

  public InputBinaryFile(File file, int[] sz, String... schema)
  { this(file, sz, CollectionUtil.asConstList(schema)); }

  public InputBinaryFile(File file, int[] sz, List<String> schema)
  {
    super(getFullSchema(schema));
    filename_ = file.getName();
    schema_size_ = schema.size();
    columns_sizes_ = Arrays.copyOf(sz, sz.length);
    columns_total_ = verifyColumnSizes(columns_sizes_, schema);
    buffer_ = new byte[columns_total_];

    rd_ = new BinaryFileReader(file);

    /* TODO
    List<String> metaschema = readMetaSchema(filename_, false);
    if (metaschema!=null) {
      for (int i=0 ; i<schema_size_ ; i++ ) {
        if (!schema.get(i).equals(metaschema.get(i))) {
          throw new SchemaException("Expected schema "+schema
              +" does not match saved metadata "+metaschema);
        }
      }
    }
    */
  }

  private static List<String> getFullSchema(List<String> schema)
  {
    List<String> ret = new ArrayList();
    ret.add(COL_FILENAME);
    ret.addAll(schema);
    return CollectionUtil.asConstList(ret);
  }

  /* TODO
  private void readMetaSchema(String filename, boolean exceptions)
  {
    String metafile = OutputFile.FILE_META_PREFIX+filename;
    try {
      List<String> in = StringUtil.readFileLines(metafile);
      for (String line : in) {
        if (line.startsWith(OutputFile.META_SCHEMA)) {
          String[] schema = line.substring(OutputFile.META_SCHEMA.length()+1).split("\t");
          return CollectionUtils.asConstList(schema);
        }
        else if (line.startsWith(OutputFile.META_BINSIZE)) {
          String[] sizesStr = line.substring(OutputFile.META_BINSIZE.length()+1).split("\t");
          int[] sizes = new int[sizesStr.length];
          for (int i=0 ; i<sizesStr.length ; ++ i) {
            try {
            }
            catch {
              thr
            }
          }
        }
      }
    }
    catch (FileNotFoundException fnfex) {
      if (exceptions) {
        throw new SchemaException(fnfex.toString());
      }
      else {
        return null;
      }
    }
    catch (IOException ioex) {
      ioex.printStackTrace();
    }

    throw new SchemaException("No row \""+OutputFile.META_SCHEMA+"\" in "+metafile);
  }
  */

  @Override
  public boolean open()
  { return rd_.open(); }

  @Override
  public void close()
  { rd_.close(); }

  @Override
  protected int readRow(String[] result)
  {
    int res = rd_.read(buffer_, 0, columns_total_);
    if (res==-1) {
      close();
      return -1;
    }
    else if (res<columns_total_) {
      int c = 0;
      while (res>0) {
        res -= columns_sizes_[c];
        ++ c;
      }
      return c;
    }

    result[0] = filename_;
    int k = 1;
    int b = 0;
    for (int c = 0 ; c < columns_sizes_.length ; ++ c) {
      int sz = columns_sizes_[c];
      if (0<=sz && sz<=8) {
        long val = 0L;
        for (int i = 0 ; i < sz ; ++ i) {
          val = (val<<8L) | (buffer_[b] & 0xFF);
          //System.out.println(Long.toHexString(val));
          ++ b;
        }
        result[k] = Long.toString(val);
        //System.out.println("*** "+Long.toHexString(val));
      }
      else {
        sz = Math.abs(sz);
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < sz ; ++ i) {
          int val = (buffer_[b] & 0xFF);
          sb.append(HEX_DIGIT[val>>4]);
          sb.append(HEX_DIGIT[val&15]);
          ++ b;
        }
        result[k] = sb.toString();
      }
      ++ k;
    }
    return k;
  }

  @Override
  protected long getInternalHashCode()
  { return filename_.hashCode(); }

  @Override
  protected String toStringWhich()
  { return rd_.toString(); }

  static int verifyColumnSizes(int[] sizes, List<String> schema)
  {
    if (sizes.length != schema.size()) {
      throw new SchemaException("Schema "+schema
          +" does not match size list "+Arrays.toString(sizes));
    }
    int total = 0;
    for (int i : sizes) {
      total += i;
      /*
      if (i >= 1 && i<=8) {
      }
      else {
        throw new SchemaException("Illegal column size: "+i);
      }
      */
    }
    return total;
  }

  public static void main(String[] args)
  {
    String[] SCHEMA = new String[] { "urlhash", "siteid", "eedbid", "skiprow" };
    int[] SIZES = new int[] { 8, 4, 4, 16 };
    Input in = new InputBinaryFile(args[0], SIZES, SCHEMA);
    Output out = new OutputFile(args[1],
        EnumSet.of(OutputFile.Flag.REPLACE, OutputFile.Flag.UNCOMPRESSED),
        SCHEMA);

    new Pusher().debug()
      .always(out)
      .push(in);
  }
}

