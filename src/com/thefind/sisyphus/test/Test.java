package com.thefind.sisyphus.test;

import com.thefind.sisyphus.SchemaIn;

/**
 * @author Eric Gaudet
 */
public interface Test
extends SchemaIn
{
  public boolean eval(String[] that)
  throws EvalException;
}

