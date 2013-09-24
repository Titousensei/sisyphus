package com.thefind.sisyphus;

import java.util.Arrays;

/**
 * @author Eric Gaudet
 */
public class OutputKeyBinding
extends Output
{
  protected final KeyBinding bind_;

  public OutputKeyBinding(KeyBinding bind)
  {
    super(bind.getSchemaIn());
    bind_ = bind;
  }

  public OutputKeyBinding(KeyBinding bind, String... schema)
  {
    super(schema);
    bind_ = bind;
  }

  @Override
  protected void append(String[] values)
  {
    try {
      long key = Long.parseLong(values[0]);
      long val = Long.parseLong(values[1]);
      bind_.put(key, val);
    }
    catch (NumberFormatException nfex) {
      warnings_ ++;
      if (warnings_<10) {
        System.out.println("[OutputKeyBinding] WARNING - NumberFormatException "
            + Arrays.toString(values) + " in " + bind_.toString());
      }
      return;
    }
  }

  @Override
  public boolean open() { return true; }

  @Override
  public void close() {}

  @Override
  public boolean sameAs(long that)
  { return (that==bind_.hashCode()); }

  @Override
  protected String toStringWhich()
  { return bind_.toString(); }
}

