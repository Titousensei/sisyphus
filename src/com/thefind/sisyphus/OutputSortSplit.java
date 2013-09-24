package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.StringUtil;
import com.thefind.util.CollectionUtil;

/*
 * Note use of adapter_.view() : always make a copy of this array otherwise it
 * is a reference and will modify all variables pointing to it un-wantedly
 *
 * @author Eric Gaudet
 * @author Rajkumar Ponnusamy
 * @author Seinjuti Chakraborty
 */
public class OutputSortSplit
extends Output
{
  protected final SchemaAdapter adapter_out_;
  protected final String filename_;
  protected final int batchsize_;
  protected final RowComparator row_comparator_;

  protected int numCols_;
  protected Output outputFile_;
  protected String[][] currentBatch_;
  protected String[] max_row_;
  protected boolean compressed_;
  protected int currentCount_;

  public OutputSortSplit(String filename, int batchsize, String[] schema_sort, String[] schema_output)
  { this(filename, batchsize, new RowComparator(schema_sort), schema_output); }

  public OutputSortSplit(String filename, int batchsize, RowComparator comp_sort, String[] schema_output)
  {
    super(schema_output);

    filename_  = filename;
    batchsize_ = batchsize;
    compressed_ = true;
    currentCount_ = 0;

    adapter_out_    = new SchemaAdapter(schema_output);
    row_comparator_ = comp_sort;
    try {
      row_comparator_.setSchemaIn1(CollectionUtil.asConstList(schema_output));
      row_comparator_.setSchemaIn2(CollectionUtil.asConstList(schema_output));
    }
    catch (SchemaException ex) {
      throw new SchemaException("Output " + Arrays.asList(schema_output)
          + " doesn't contain columns from "+row_comparator_);
    }

    FileGroup group = new FileGroup("", "regex:"+filename_+"\\.[0-9]+");
    for (File f : group.files_) {
      if (f.delete()) {
        if (verbose_) {
          System.err.println("[OutputSortSplit] Deleted existing file: "+f.getAbsolutePath());
        }
      }
      else {
        System.err.println("[OutputSortSplit] WARNING - Failed to delete file: "+f.getAbsolutePath());
      }
    }
  }

  public void setCompressed(boolean compressed)
  {
    compressed_ = compressed;
    if (verbose_) {
      System.err.println("[OutputSortSplit] setCompressed \""+filename_+"\" "+compressed_);
    }
  }

  @Override
  protected void readySchema(List<String> schema_row)
  {
    adapter_out_.setSchemaIn(schema_row);
    numCols_ = schema_row.size();
    currentBatch_ = new String[batchsize_][numCols_];
    currentCount_ = 0;
  }

  /**
   * 1. sort the batch based on the ordered keys in place
   * 2. flush the current batch
   * 3. remember the max keys seen for each ordering key
   * 4. increment the slicenum i.e suffix of output file
   */
  protected void flushBatch(boolean closeSplit)
  {
    //starting a new batch/slice if necessary
    if (outputFile_ == null ) {
      outputFile_ = new OutputFile(filename_,
                                   compressed_
                                     ? EnumSet.of(OutputFile.Flag.SUFFIX_INCREMENT)
                                     : EnumSet.of(OutputFile.Flag.UNCOMPRESSED, OutputFile.Flag.SUFFIX_INCREMENT),
                                   adapter_out_.getSchemaOut());
      outputFile_.open();
    }

    if (currentCount_> 0) {
      long t0 = System.currentTimeMillis();

      // 1. sort the batch based on the ordered keys in place
      Arrays.sort(currentBatch_, 0, currentCount_, row_comparator_);

      // 2. flush the current batch
      synchronized (this) {
        for (int i=0 ; i<currentCount_ ; i++) {
          outputFile_.useDirect(currentBatch_[i]);
        }
      }

      long t1 = System.currentTimeMillis();

      if (debug_) {
        String startRow = StringUtil.join(currentBatch_[0], ",");
        String endRow   = StringUtil.join(currentBatch_[currentCount_-1], ",");

        System.err.println("[OutputSortSplit] Flushed "+outputFile_
            +String.format(" %,d rows - ", currentCount_)
            +StringUtil.readableTime(t1-t0)+" ("
            +(((t1-t0)>0)
              ? Math.round(60.*currentCount_/(t1-t0)) +"K)"
              : "inf.)")
            +" rpm\n- begin ["+startRow+"]\n- end ["+endRow+"] ");
      }
      else if (verbose_) {
        System.err.println("[OutputSortSplit] Flushed "+outputFile_
            +String.format(" %,d rows - ", currentCount_)
            +StringUtil.readableTime(t1-t0)+" ("
            +(((t1-t0)>0)
              ? Math.round(60.*currentCount_/(t1-t0)) +"K)"
              : "inf.)")
            +" rpm");
      }
    }

    // 3. remember the max keys seen for each ordering key
    // 4. increment the slicenum i.e suffix of output file
    if (closeSplit) {
      outputFile_.close();
      outputFile_ = null;
      max_row_ = null;
    }
    else {
      max_row_ = new String[numCols_];
      System.arraycopy(currentBatch_[currentCount_-1], 0, max_row_, 0, numCols_);
    }

    currentCount_ = 0;
  }

  @Override
  protected void append(String[] row)
  {
    String[] out_row = adapter_out_.getView(row);

    //On encountering a smaller key than the max_row_ flush the current batch and start a new split
    if ((max_row_!=null)
    && (row_comparator_.compare(max_row_, out_row) > 0)
    ) {
      // if a smaller key comes up flush the ongoing batch if there exists
      // anything inside the batch and start again with new max row
      flushBatch(true);
    }
    else if (currentCount_ >= batchsize_) {
      //System.err.println("DEBUG - Flushing batch");
      flushBatch(false);
    }

    //explicitly copy: reference is passed around & overwritten
    System.arraycopy(out_row, 0, currentBatch_[currentCount_], 0, out_row.length);
    currentCount_++;
  }

  @Override
  public boolean open()
  { return true; }

  @Override
  public void close()
  {
    //System.err.println("Flushing last batch....");
    flushBatch(true);
  }

  @Override
  public boolean sameAs(long that)
  { return (filename_!=null) && (that==filename_.hashCode()); }

  @Override
  protected String toStringWhich()
  {
    return "\""+filename_+".<split>\" sorted by "+row_comparator_;
  }

  public static void sortOneFile(String input_file, String output_file,
      int batch_size, String[] sort_schema, String[] file_schema)
  {
    Input in_rows = new InputFile(input_file, file_schema);

    Output sort = new OutputSortSplit(output_file, batch_size,
                          sort_schema, file_schema);

    System.err.println("[OutputSortSplit.sortOneFile] sorting: "+in_rows+" into: "+sort);
    new Pusher()
        .always(sort)
        .push(in_rows);
  }
}
