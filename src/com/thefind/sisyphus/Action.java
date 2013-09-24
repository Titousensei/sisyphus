package com.thefind.sisyphus;

import java.util.List;

/**
 * @author Eric Gaudet
 */
interface Action
extends SchemaIn, SchemaOut
{
  public boolean open();

  public List<String> getSchemaIn();

  public List<String> getSchemaOut();

  public void ready(List<String> schema_row);

  public void use(String[] row)
  throws InterruptException;

  public void useParallel(String[] row)
  throws InterruptException;

  public boolean sameAs(long that);

  public void close();
}

