package com.thefind.sisyphus;

import java.util.*;

import com.thefind.sisyphus.test.*;
import com.thefind.util.TaskDispatch;
import com.thefind.util.TaskDispatch.Task;
import com.thefind.util.PerformanceMonitor;
import com.thefind.util.StringUtil;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class Pusher
{
  protected final static String DEFAULT_NAME = "Pusher";
  protected final static String VERSION = "0.3.1";

  protected final List<Action> actions_ = new ArrayList();

  protected boolean profiler_ = false;
  protected boolean debug_    = false;
  protected int debug_lines_ = 0;
  protected int parallel_     = 1;

  protected boolean skip_too_long_  = false;
  protected boolean skip_too_short_ = false;

  protected final String name_;
  protected final int limit_;

  protected final double sample_;
  protected final boolean sampling_;
  protected final Random rand_ = new Random();

  public Pusher()                           { this(null, 0,     0.0); }
  public Pusher(int limit)                  { this(null, limit, 0.0); }
  public Pusher(double sample)              { this(null, 0,     sample); }
  public Pusher(int limit, double sample)   { this(null, limit, sample); }
  public Pusher(String name)                { this(name, 0,     0.0); }
  public Pusher(String name, int limit)     { this(name, limit, 0.0); }
  public Pusher(String name, double sample) { this(name, 0,     sample); }

  public Pusher(String name, int limit, double sample)
  {
    name_ = (name!=null)
            ? DEFAULT_NAME + ":" + name
            : DEFAULT_NAME;
    System.err.println("["+name_+"] Sisyphus Version "+VERSION);

    limit_ = limit;
    if (limit_>0) {
      System.err.println(String.format("[%s] LIMIT Inputs = %,d", name_, limit_));
    }

    sample_ = sample;
    if (1.0>sample_ && sample_>0.0) {
      System.err.println("["+name_+"] SAMPLING Inputs @"+sample_);
      sampling_ = true;
    }
    else {
      sampling_ = false;
    }
  }

  public void push(Input... inputs)
  {
    int num_actions = actions_.size();
    long[] prof = new long[num_actions];
    long prof_in = 0L;
    int total_row_count = 0;
    TaskDispatch td = null;
    if (parallel_>1) {
      td = new TaskDispatch(name_, parallel_, TaskDispatch.Verbose.DISABLED);
    }

    for (int i=0 ; i<num_actions ; i++) {
      Action act = actions_.get(i);
      prof[i] = 0L;
      act.open();
      System.err.println("["+name_+"] open: "+act);
      for (Input in : inputs) {
        if (in.sameAs(act)) {
          System.err.println("["+name_+"] WARNING - Output into Input: "+act+" <=> "+in);
        }
      }
    }

    PerformanceMonitor mon = new PerformanceMonitor();
    for (int z=0 ; z<inputs.length ; z++) {
      Input in = inputs[z];
      System.err.println("["+name_+"] Start --- "+in);
      if (in.open()) {
        long t0 = System.currentTimeMillis();
        List<String> schema_row = new ArrayList(in.getSchemaOut());
        for (int i=0 ; i<num_actions ; i++) {
          Action act = actions_.get(i);
          List<String> out = act.getSchemaOut();
          if (out!=null) {
            for (String col : out) {
              if (!schema_row.contains(col)) {
                schema_row.add(col);
              }
              else {
                System.err.println("["+name_+"] WARNING - Existing column ["+col+"] is modified by "+act);
              }
            }
          }
          if (debug_) System.err.println("["+name_+"] ... DEBUG - ready Action "+i+": "+act);
          act.ready(schema_row);
        }
        in.ready(schema_row);
        System.err.println("["+name_+"] ... row schema: "+schema_row);

        int line_count = 0;
        int debug_counter = debug_lines_;
        debug_ = (debug_lines_>0);
        int num_ex = 0;
        try {
          while (limit_==0 || line_count<limit_) {
            String[] row;
            if (profiler_) {
              long p0 = System.nanoTime();
              row = in.getRow();
              while (in.isRowEmpty()
              || (skip_too_short_ && in.isRowTooShort())
              || (skip_too_long_  && in.isRowTooLong())
              ) {
                row = in.getRow();
              }
              long p1 = System.nanoTime();
              prof_in += (p1-p0);
            }
            else {
              row = in.getRow();
              while (in.isRowEmpty()
              || (skip_too_short_ && in.isRowTooShort())
              || (skip_too_long_  && in.isRowTooLong())
              ) {
                row = in.getRow();
              }
            }
            if (row == null) {
              break;
            }
            if (sampling_ && rand_.nextDouble()>=sample_) {
              continue;
            }
            line_count ++;

            if (parallel_>1) {
              td.push(new RowTask(actions_, row));
            }
            else {
              try {
                if (profiler_) {
                  for (int i=0 ; i<num_actions ; i++) {
                    long p0 = System.nanoTime();
                    actions_.get(i).use(row);
                    long p1 = System.nanoTime();
                    prof[i] += (p1-p0);
                  }
                }
                else {
                  for (int i=0 ; i<num_actions ; i++) {
                    actions_.get(i).use(row);
                  }
                }
              }
              catch (InterruptException iex) {
                if (debug_) System.err.println("["+name_+"] ... DEBUG - Interrupted by "+iex.getMessage());
              }
              catch (Exception ex) {
                num_ex ++;
                if (num_ex<10) {
                  System.err.println("["+name_+"] ... EXCEPTION - row "+line_count+" "+Arrays.toString(row)+" - "+in.getRowSize()+" columns");
                  ex.printStackTrace();
                }
              }
            }

            if ((line_count % 1000000) == 0) {
              long t1 = System.currentTimeMillis();
              System.err.println("["+name_+"] ... "+(line_count/1000000)+"M - "+StringUtil.readableTime(t1-t0));
              if (debug_lines_==-1) System.err.println("["+name_+"] ... DEBUG - row "+line_count+" "+Arrays.toString(row)+" - "+in.getRowSize()+" columns");
              mon.measure();
            }
            else if ( (line_count<1000000)
            && ((line_count == 300000) || (line_count == 100000)
               || (line_count == 30000)  || (line_count == 10000)
               || (line_count == 3000)   || (line_count == 1000))
            ) {
              System.err.println("["+name_+"] ... "+(line_count/1000)+"K");
              if (debug_lines_==-1) System.err.println("["+name_+"] ... DEBUG - row "+line_count+" "+Arrays.toString(row)+" - "+in.getRowSize()+" columns");
              mon.measure();
            }

            if (debug_) System.err.println("["+name_+"] ... DEBUG - row "+line_count+" "+Arrays.toString(row)+" - "+in.getRowSize()+" columns");
            if (debug_counter>0) {
              if (debug_counter==1) debug_ = false;
              debug_counter--;
            }
          }
        }
        finally {
          in.close();
          if (parallel_>1 && z==inputs.length-1) {
            System.err.println("["+name_+"] Last input finished: waiting for threads");
            td.join();
            td = null;
          }
          if (num_ex>0) {
            System.err.println("["+name_+"] WARNING: "+num_ex+" exceptions (see stack traces above)");
          }
        }
        mon.measure();

        long t1 = System.currentTimeMillis();
        double rpm = ((t1-t0)>0) ? 60.*line_count/(t1-t0) : 0.0;
        System.err.print("["+name_+"] Done  --- "+in+" - "
            +String.format("%,d", line_count)+" rows - "
            +StringUtil.readableTime(t1-t0)+" ("
            +((rpm>=2000.0)
              ? String.format("%,dK rpm)", Math.round(rpm))
              : (rpm>0.0)
                ? String.format("%,d rpm)", Math.round(1000.0*rpm))
                : "inf. rpm)")
            );
        if (profiler_) {
          System.err.print(" - profiler: ");
          System.err.print(Math.round((1.0*prof_in)/line_count)/1000.0);
          System.err.print(" us/row");
        }
        System.err.println();
        total_row_count += line_count;
        System.err.println("["+name_+"] "+mon.toString());
      }
      else {
        System.err.println("["+name_+"] WARNING - Could not open: "+in);
      }
    }

    for (int i=0 ; i<num_actions ; i++) {
      actions_.get(i).close();
      System.err.print("["+name_+"] close: "+actions_.get(i));
      if (profiler_ && total_row_count>0) {
        System.err.print(" - profiler: ");
        System.err.print(Math.round((1.0*prof[i])/total_row_count)/1000.0);
        System.err.print(" us/row");
      }
      System.err.println();
    }
    if (td!=null) {
      td.join();
      td = null;
    }
  }

  /* BUILDING METHODS */

  public Pusher debug()
  {
    debug_ = true;
    debug_lines_ = Integer.MAX_VALUE;
    System.err.println("["+name_+"] DEBUG enabled, will print all rows");
    return this;
  }

  public Pusher parallel(int val)
  {
    parallel_ = val;
    if (parallel_>1) {
      System.err.println("["+name_+"] PARALLEL set to "+parallel_+": row order and concurrent read-modify-copy not guaranteed");
    }
    else {
      System.err.println("["+name_+"] PARALLEL disabled");
    }
    return this;
  }


  public Pusher profiler()
  {
    profiler_ = true;
    System.err.println("["+name_+"] PROFILER enabled");
    return this;
  }

  public Pusher debug(int num_lines)
  {
    debug_ = true;
    if (num_lines>0) {
      System.err.println("["+name_+"] DEBUG enable, will print first "+num_lines+" rows");
    }
    else if (num_lines==-1) {
      System.err.println("["+name_+"] DEBUG enable, will print rows for progress markers");
    }
    else {
      System.err.println("["+name_+"] DEBUG enabled, will not print rows");
    }
    debug_lines_ = num_lines;

    return this;
  }

  public Pusher skipBadRows()
  {
    skip_too_short_ = true;
    skip_too_long_  = true;
    return this;
  }

  public Pusher skipLongRows()
  { skip_too_long_ = true; return this; }

  public Pusher skipShortRows()
  { skip_too_short_ = true; return this; }

  public Pusher always(Action act)
  {
    actions_.add(act);
    return this;
  }

  public Pusher onlyIf(Test tst, Action dest)
  { return always(new ActionTest(tst, dest)); }

  public Pusher ifFound(Key src, Action dest)
  { return always(new ActionIfKey(src, true, dest)); }

  public Pusher ifMiss(Key src, Action dest)
  { return always(new ActionIfKey(src, false, dest)); }

  public Pusher ifMatch(KeyMap src, Action dest)
  { return always(new ActionIfKeyMap(src, true, dest)); }

  public Pusher ifDiff(KeyMap src, Action dest)
  { return always(new ActionIfKeyMap(src, false, dest)); }

  public Pusher ifDiff(KeyBinding src, Action dest)
  { return always(new ActionIfKeyBinding(src, false, dest)); }

  public Pusher ifMatch(KeyBinding src, Action dest)
  { return always(new ActionIfKeyBinding(src, true, dest)); }

  public Pusher ifDiff(KeyDouble src, Action dest)
  { return always(new ActionIfKeyDouble(src, false, dest)); }

  public Pusher ifMatch(KeyDouble src, Action dest)
  { return always(new ActionIfKeyDouble(src, true, dest)); }

  public Pusher ifNull(String col, Action dest)
  { return always(new ActionIfNull(col, true, dest)); }

  public Pusher ifNotNull(String col, Action dest)
  { return always(new ActionIfNull(col, false, dest)); }

  /** Composites **/

  public Pusher ifDup(KeyDouble src, Action dest)
  {
    ifFound(src, dest);
    always(new OutputKeyDouble(src));
    return this;
  }

  public Pusher ifDup(KeyBinding src, Action dest)
  {
    ifFound(src, dest);
    always(new OutputKeyBinding(src));
    return this;
  }

  public Pusher ifDup(KeyMap src, Action dest)
  {
    ifFound(src, dest);
    always(new OutputKeyMap(src));
    return this;
  }

  public Pusher ifDup(Key src, Action dest)
  {
    ifFound(src, dest);
    always(new OutputKey(src));
    return this;
  }

  protected static class RowTask
  implements Task
  {
    private final List<Action> actions_;
    private final String[] row_;

    protected RowTask(List<Action> actions, String[] row)
    {
      actions_ = actions;
      int l = row.length;
      row_ = new String[l];
      System.arraycopy(row, 0, row_, 0, l);
    }

    @Override
    public boolean process()
    {
      int num_actions = actions_.size();
      try {
        for (int i=0 ; i<num_actions ; i++) {
          actions_.get(i).useParallel(row_);
        }
        return true;
      }
      catch (InterruptException iex) {
        // ignore
        //System.err.println("BREAK - "+Arrays.toString(row_));
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
      return false;
    }
  }
}

