package com.thefind.sisyphus;

import java.util.*;

import com.thefind.util.CollectionUtil;

/**
 * A class to load data split by columns in different files.
 * Each row is composed of the union of corresponding rows in each file.
 *
 * @author Eric Gaudet
 */
public class InputColumns
extends Input
{
  protected final List<Input> inputs_;
  protected final List<SchemaAdapter> adapters_;

  protected InputColumns(Input... inputs)
  { this(CollectionUtil.asConstList(inputs)); }

  protected InputColumns(List<Input> inputs)
  {
    super(mergeSchemas(inputs));
    inputs_ = CollectionUtil.asConstList(inputs);
    adapters_ = new ArrayList(inputs_.size());
    for (Input in : inputs_) {
      adapters_.add(new SchemaAdapter(in.getSchemaOut()));
    }
  }

  private static List<String> mergeSchemas(List<Input> inputs)
  {
    List<String> ret = new ArrayList();
    for (Input in : inputs) {
       for (String col : in.getSchemaOut()) {
         if (ret.contains(col)) {
           throw new SchemaException("Column exists in several Inputs: " + col);
         }
         ret.add(col);
       }
    }
    return ret;
  }

  @Override
  public final boolean open()
  {
    for (Input in : inputs_) {
      if (!in.open()) {
        System.err.println("[InputColumns] ERROR - Could not open "+in);
        return false;
      }
    }

    return true;
  }

  @Override
  public final void ready(List<String> schema_row)
  {
    super.ready(schema_row);

    for (Input in : inputs_) {
      in.ready(in.getSchemaOut());
    }

    for (SchemaAdapter ad : adapters_) {
      ad.setSchemaIn(schema_row);
    }
  }

  @Override
  public final void close()
  {
    for (Input in : inputs_) {
      in.close();
    }
  }

  @Override
  protected final int readRow(String[] result)
  {
    Iterator<Input> it_in = inputs_.iterator();
    Iterator<SchemaAdapter> it_ad = adapters_.iterator();

    int sz = 0;
    while (it_in.hasNext()) {
      Input in = it_in.next();
      SchemaAdapter ad = it_ad.next();

      String[] row = in.getRow();
      sz += in.getRowSize();
      ad.applyView(result);
    }

    return sz;
  }

  @Override
  public final long getInternalHashCode()
  {
    long ret = 0;
    for (Input in : inputs_) {
      ret ^= in.getInternalHashCode();
    }
    return ret;
  }

  @Override
  public final boolean sameAs(Action act)
  {
    for (Input in : inputs_) {
      if (in.sameAs(act)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected final String toStringWhich()
  { return inputs_.toString(); }
}

