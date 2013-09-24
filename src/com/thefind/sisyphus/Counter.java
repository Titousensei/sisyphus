package com.thefind.sisyphus;

import java.util.*;

/**
 * @author Eric Gaudet
 */
public class Counter
implements Action
{
  protected final String label_;
  protected int count_;
  protected int debug_count_;

  public Counter(String label)
  { this(label, 0); }

  public Counter(String label, int debug_count)
  {
    label_ = label;
    debug_count_ = debug_count;
  }

  public boolean open()
  {
    count_ = 0;
    return true;
  }

  public void reset()
  { count_ = 0; }

  public int getCount()
  { return count_; }

  @Override
  public List<String> getSchemaIn()
  { return null; }

  public List<String> getSchemaOut()
  { return null; }

  public void ready(List<String> schema_row) {}

  public void use(String[] row)
  throws InterruptException
  {
    count_++;
    if (count_<debug_count_) {
      System.err.println("[Counter] DEBUG \""+label_+"\" - "+Arrays.asList(row).toString());
    }
  }

  public void useParallel(String[] row)
  throws InterruptException
  {
    synchronized (this) {
      count_++;
    }
    if (count_<debug_count_) {
      System.err.println("[Counter] DEBUG \""+label_+"\" - "+Arrays.asList(row).toString());
    }
  }

  public boolean sameAs(long that)
  { return false; }

  public void close()
  { System.err.println("[Counter] Final value --- \""+label_+"\" : "+String.format("%,d", count_)); }

  public String toString()
  { return "Counter\""+label_+'"'; }
}

