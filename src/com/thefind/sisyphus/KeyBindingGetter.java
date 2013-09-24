package com.thefind.sisyphus;

/**
 * @author Eric Gaudet
 */
public class KeyBindingGetter
extends Modifier
{
  protected final KeyBinding bind_;

  public KeyBindingGetter(KeyBinding bind)
  {
    super(new String[] { bind.getSchemaIn().get(0) }, new String[] { bind.getSchemaIn().get(1) });
    bind_ = bind;
  }

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

