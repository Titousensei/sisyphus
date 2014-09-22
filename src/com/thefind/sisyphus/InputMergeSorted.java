package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.CollectionUtil;

import static com.thefind.sisyphus.InputFile.*;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class InputMergeSorted
extends Input
{
  protected final FileGroup group_;
  protected final int nb_files_;

  protected final int schema_length_;
  protected final List<String> schema_sort_;
  protected final EnumSet<Flag> flags_;

  protected final RowComparator row_comparator_;

  //merge related data structures
  protected final Input[] inputs_;

  public static enum Flag { IGNORE };

  // TODO: Change the constructor schema to match InputJoinSorted:
  // (String[] join_schema, Input... inputs)

  public InputMergeSorted(String dirname, String filter,
      String[] schema_sort, String[] schema_row)
  { this(dirname, filter, EnumSet.noneOf(Flag.class), schema_sort, schema_row); }

  public InputMergeSorted(String dirname, String filter, Flag flag,
      String[] schema_sort, String[] schema_row)
  { this(dirname, filter, EnumSet.of(flag), schema_sort, schema_row); }

  public InputMergeSorted(String dirname, String filter, EnumSet<Flag> flags,
      String[] schema_sort, String[] schema_row)
  {
    super(getFullSchema(schema_row));

    flags_          = flags;
    group_          = new FileGroup(dirname, filter);
    nb_files_       = group_.files_.length;

    schema_sort_    = CollectionUtil.asConstList(schema_sort);
    schema_length_  = schema_.size();

    row_comparator_ = new RowComparator(schema_sort_);
    row_comparator_.setSchemaIn1(schema_);
    row_comparator_.setSchemaIn2(schema_);
    inputs_ = new Input[nb_files_];
    for (int i=0 ; i<nb_files_ ; i++) {
      inputs_[i] = new InputFile(group_.files_[i], schema_row);
    }
  }

  private static List<String> getFullSchema(String... schema)
  {
    List<String> ret = new ArrayList(schema.length+1);
    ret.add(COL_FILENAME);
    for (int i=0 ; i<schema.length ; i++) {
      ret.add(schema[i]);
    }
    return CollectionUtil.asConstList(ret);
  }

  //TODO : implement read meta_schema methods
  //TODO : implement flag=IGNORE

  @Override
  public boolean open()
  {
    boolean haveOne = false;
    for (int i=0 ; i<nb_files_ ; i++) {
      if (inputs_[i].open()) {
        inputs_[i].ready(schema_);
        String[] row = inputs_[i].getRow();
        if (row!=null) {
          haveOne = true;
        }
      }
    }

    return haveOne;
  }

  @Override
  public void close()
  {
    for (Input in : inputs_) {
      in.close();
      System.err.println("[InputMergeSorted] close: "+in.toString()+" - "
          +String.format("%,d", in.getLines()) + " rows");
    }
  }

  @Override
  protected int readRow(String[] result)
  {
    // find next line to push
    int best = -1;
    String[] bestRow = null;
    for (int i=0 ; i < nb_files_ ; i++) {
      bestRow = inputs_[i].getCurrentRow();
      if (bestRow!=null) {
        best = i;
        break;
      }
    }
    if (bestRow==null) return -1;

    for (int i=best+1 ; i < nb_files_ ; i++) {
      String[] row = inputs_[i].getCurrentRow();
      if (row!=null) {
        int comp = row_comparator_.compare(bestRow, row);
        if (comp>0) {
          bestRow = row;
          best = i;
        }
      }
    }

    System.arraycopy(bestRow, 0, result, 0, schema_length_);

    // advance this file, ignore the returned row for now
    inputs_[best].getRow();
    return result.length;
  }

  @Override
  public long getInternalHashCode()
  { return group_.str_.hashCode(); }

  @Override
  public boolean sameAs(Action act)
  { return group_.sameAs(act); }

  @Override
  protected String toStringWhich()
  { return group_.toString(); }
}

