package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
public class KeyBindingGetter
extends Modifier
{
  protected final KeyBinding bind_;

  public KeyBindingGetter(KeyBinding bind)
  { this(bind, bind.getSchemaIn().get(0), bind.getSchemaIn().get(1)); }

  /**
   * Use these constructors only if the query schema doesn't match the KeyBinding schema.
   * This is for the rare case where you have several key columns with different names,
   * and you want to populate corresponding value columns, also with different names.
   */
  public KeyBindingGetter(KeyBinding bind, String col_key, String col_value)
  {
    super(new String[] { col_key }, new String[] { col_value });
    bind_ = bind;
  }

  public KeyBindingGetter(KeyBinding bind, String col_value)
  { this(bind, bind.getSchemaIn().get(0), col_value); }

  @Override
  public void compute(String[] input, String[] result)
  {
    try {
      long key   = Long.parseLong(input[0]);
      long value = bind_.get(key);
      if ((value != KeyBinding.NULL_BIND) || (bind_.contains(key))) {
        result[0] = Long.toString(value);
      }
    } catch (NumberFormatException nfex) {
      result[0] = null;
    }
  }
}

