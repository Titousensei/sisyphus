package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public abstract class Modifier
implements Action
{
  protected final SchemaAdapter in_adapter_;
  protected final SchemaAdapter out_adapter_;
  protected int num_exceptions_;
  protected int num_warnings_;

  public Modifier(String[] incol, String[] outcol)
  {
    in_adapter_  = new SchemaAdapter(incol);
    out_adapter_ = new SchemaAdapter(outcol);
    num_exceptions_ = 0;
    num_warnings_   = 0;
  }

  public Modifier(String[] col)
  { this(col, col); }

  public List<String> getSchemaIn()  { return in_adapter_.getSchemaOut(); }

  public List<String> getSchemaOut() { return out_adapter_.getSchemaOut(); }

  public abstract void compute(String[] input, String[] result) throws Exception;

  @Override
  public boolean open() { return true; }

  @Override
  public void close() {}

  @Override
  public void ready(List<String> schema_row)
  {
    in_adapter_.setSchemaIn(schema_row);
    out_adapter_.setSchemaIn(schema_row);
  }

  @Override
  public void use(String[] row)
  throws InterruptException
  {
    try {
      compute(in_adapter_.getView(row), out_adapter_.getContainer());
      out_adapter_.applyView(row);
    }
    catch (Exception ex) {
      num_exceptions_ ++;
      if (num_exceptions_<10) {
        System.err.println(getClass().getSimpleName()+" Modifier Exception using view "+Arrays.toString(in_adapter_.getView(row)));
        ex.printStackTrace();
      }
    }
  }

  @Override
  public void useParallel(String[] row)
  throws InterruptException
  {
    try {
      String[] in_copy  = in_adapter_.newContainer();
      String[] out_copy = out_adapter_.newContainer();
      in_adapter_.getView(in_copy, row);
      compute(in_copy, out_copy);
      out_adapter_.applyView(out_copy, row);
    }
    catch (Exception ex) {
      num_exceptions_ ++;
      if (num_exceptions_<10) {
        System.err.println(getClass().getSimpleName()+" Modifier Exception using view "+Arrays.toString(in_adapter_.getView(row)));
        ex.printStackTrace();
      }
    }
  }

  protected void warning(String message)
  {
    num_warnings_ ++;
    if (num_warnings_<10) {
      System.err.println(getClass().getSimpleName() + " WARNING - " + message);
    }
  }

  @Override
  public boolean sameAs(long that)
  { return false; }

  public String toStringModif()
  { return ""; }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName())
      .append('{')
      .append(in_adapter_.getSchemaOut())
      .append(toStringModif())
      .append(" -> ")
      .append(out_adapter_.getSchemaOut());
    if (num_warnings_>0) {
      sb.append(", ")
        .append(String.format("%,d", num_warnings_))
        .append(" warnings");
    }
    if (num_exceptions_>0) {
      sb.append(", ")
        .append(String.format("%,d", num_exceptions_))
        .append(" exceptions");
    }
    sb.append('}');
    return sb.toString();
  }
}

