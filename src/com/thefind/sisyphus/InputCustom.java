package com.thefind.sisyphus;

import java.io.EOFException;
import java.util.*;

/**
 * @author Eric Gaudet
 */
public abstract class InputCustom
extends Input
{
  protected final static EOFException EOF_EXIT = new EOFException();
  protected final Input input_;

  protected final SchemaAdapter in_adapter_;

  public InputCustom(String[] in_cols, Input input, String[] out_cols)
  {
    super(out_cols);

    input_ = input;
    in_adapter_ = new SchemaAdapter(in_cols);
  }

  @Override
  public final boolean open()
  {
    if (!input_.open()) {
      System.err.println("[InputCustom] ERROR - Could not open "+input_);
      return false;
    }

    return true;
  }

  @Override
  public final void ready(List<String> schema_row)
  {
    super.ready(schema_row);

    input_.ready(input_.getSchemaOut());
    in_adapter_.setSchemaIn(input_.getSchemaOut());
  }

  @Override
  public final void close()
  { input_.close(); }

  protected final String[] readInput()
  throws EOFException
  {
    String[] row = input_.getRow();

    if (row==null) {
      throw EOF_EXIT;
    }

    return in_adapter_.getView(row);
  }

  /**
   * @throws EOF_EXIT
   */
  protected abstract void computeRow(String[] result)
  throws EOFException;

  @Override
  protected final int readRow(String[] result)
  {
    try {
      computeRow(result);
      return result.length;
    }
    catch (EOFException eofex) {
      return -1;
    }
  }

  @Override
  protected final long getInternalHashCode()
  { return input_.toString().hashCode(); }

  @Override
  public final boolean sameAs(Action act)
  { return input_.sameAs(act); }

  @Override
  protected final String toStringWhich()
  { return input_.toString() + " ~> " + in_adapter_.getSchemaOut(); }
}

