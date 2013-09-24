package com.thefind.util;

import java.io.*;
import java.util.concurrent.*;

/**
 * Utility to easily create a pool of worker threads that pick-up tasks
 * as they come. Each thread will call the process() method from your existing
 * class. Additionally, System.out and System.err of each thread are stored
 * in the thread's output buffer until it finishes the task. This avoids mixing
 * the prints of the different threads.
 *
 * To use TaskDispatch, simply have your class implement Task, which is an
 * interface with a single method:
 *       public boolean process();
 *
 * Then, create a TaskDispatch object, that will create the thread pool
 * of the size you want, as well as a task queue to feed the threads.
 *
 * Finally, in the loop where you serially process your data, instead pass
 * your class implementing Task to your TaskDispatch, and let the threads
 * distribute the workload for you.
 *
 *
 * Example: Original sequential code
 * <pre>
 * MyClass {
 *
 *   public void process() {
 *     // do the real work here
 *   }
 *
 *   main() {
 *     for(String params : test_suite) {
 *       MyClass cl = new MyClass(params);
 *       cl.process();
 *     }
 *   }
 * }
 * </pre>
 *
 * Example: Multi-threaded code using TaskDispatch
 * <pre>
 * import base.utils.TaskDispatch;
 * import base.utils.TaskDispatch.Task;
 *
 * MyClass implements Task {
 *
 *   \\@Override
 *   public void process() {
 *     // do the real work here
 *   }
 *
 *   main() {
 *     TaskDispatch td = new TaskDispatch(NUM_THREADS);
 *     for(String params : test_suite) {
 *       MyClass cl = new MyClass(params);
 *       td.push(cl);
 *     }
 *     td.join();
 *   }
 * }
 * </pre>
 *
 * @author Eric Gaudet
 */
public class TaskDispatch
extends ByteArrayOutputStream
{
  protected final PrintStream real_out_;
  protected PrintStream real_err_;

  protected final BlockingQueue<Task> task_queue_;

  protected final String label_;
  protected final int num_threads_;
  protected final TaskRunner[] threads_;

  protected final boolean verbose_;

  public static enum Verbose { DISABLED, ENABLED }

  public TaskDispatch(int num_threads)
  { this("TaskDispatch", num_threads, Verbose.ENABLED); }

  public TaskDispatch(int num_threads, Verbose v)
  { this("TaskDispatch", num_threads, v); }

  /**
   * Creates a fully-initialized TaskDispatch object,
   * with thread pool and task queue.
   */
  public TaskDispatch(String label, int num_threads, Verbose v)
  {
    super();
    verbose_ = (v==Verbose.ENABLED);
    label_ = label;
    num_threads_ = num_threads;
    task_queue_  = new ArrayBlockingQueue(num_threads*num_threads+3, true); // fair

    real_out_ = System.out;
    real_err_ = System.err;

    System.err.println("["+label_+"] START - "+num_threads_+" threads ...");
    threads_ = new TaskRunner[num_threads_];
    for (int i=0 ; i<num_threads_ ; i++ ) {
      String name = label_+"-"+i;
      threads_[i] = new TaskRunner(name);
      threads_[i].start();
      System.err.println("["+label_+"] ... begin "+name);
    }

    System.err.println("["+label_+"] All "+num_threads_+" threads are running");

    if (verbose_ && num_threads_>1) {
      PrintStream tosd = new PrintStream(this, true);
      System.setOut(tosd);
      System.setErr(tosd);
    }
  }

  public int getQueueSize()
  { return task_queue_.size(); }

  /**
   * Push a Task to the queue for the threads to pick and execute.
   */
  public void push(Task t)
  {
    try {
      task_queue_.put(t);
    }
    catch (InterruptedException iex) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Let all the threads finish and close this TaskDispatch.
   */
  public void join()
  { join(0); }

  public void join(int timeoutms)
  {
    System.err.println("["+label_+"] JOIN - Closing "+num_threads_+" threads");
    for (int i=0 ; i<num_threads_ ; i++) {
      push(NULL_TASK);
    }
    int num_success = 0;
    int num_processed = 0;
    for (int i=0 ; i<num_threads_ ; i++) {
      try {
        if (threads_[i].isAlive()) {
          System.err.println("["+label_+"] Waiting for: "+threads_[i].getName());
          threads_[i].join(timeoutms);
          if (threads_[i].isAlive()) {
            threads_[i].interrupt();
          }
        }
        num_success += threads_[i].getSuccess();
        num_processed += threads_[i].getProcessed();
        System.err.println(String.format("["+label_+"] Closed: %s (%,d / %,d processed)",
            threads_[i].getName(), threads_[i].getSuccess(), threads_[i].getProcessed()));
      }
      catch (InterruptedException iex) {
        Thread.currentThread().interrupt();
      }
    }

    System.setOut(real_out_);
    System.setErr(real_err_);
    System.err.println(String.format("["+label_+"] DONE - All %d threads are closed (%,d / %,d processed)",
        num_threads_, num_success, num_processed));
  }

  /**
   * Output a string to the real PrintStream, not the thread's buffer.
   */
  public final void flushOut(String in)
  {
    if (verbose_) {
      real_out_.print(in);
      real_out_.flush();
    }
  }

  /**
   * Override the ByteArrayOutputStream's method to redirect the system outs
   * to the buffer of the thread which did the print.
   */
  public void flush()
  throws IOException
  {
    String record;
    synchronized (this) {
        super.flush();
        record = this.toString();
        super.reset();

        if (record.length() == 0) {
            // avoid empty records
            return;
        }

        Thread dest = Thread.currentThread();
        if (dest instanceof TaskRunner) {
          ((TaskRunner) dest).print(record);
        }
        else {
          flushOut(record);
        }
    }
  }

  public int getNumThreads()
  { return num_threads_; }

  /**
   * The interface for the Task type.
   */
  public interface Task
  {
    /**
     * @return whether the process was a success or not.
     */
    public boolean process();
  }

  protected final static NullTask NULL_TASK = new NullTask();

  /**
   * NullTask is used as a termination message to the threads.
   */
  protected static class NullTask
  implements Task
  {
    public boolean process() { return true; }
  }

  /**
   * The Thread in the pool that will pick-up and execute Task jobs.
   */
  protected class TaskRunner
  extends Thread
  {
    protected StringBuilder out_;
    protected int processed_;
    protected int success_;

    public TaskRunner(String name)
    {
      super();
      setName(name);
      processed_ = 0;
      success_ = 0;
    }

    public void print(String in)
    { out_.append(in); }

    public int getProcessed()
    { return processed_; }

    public int getSuccess()
    { return success_; }

    public void run()
    {
      while (true) {
        Task t = null;
        out_ = null;
        try {
          t = task_queue_.take();
          if (t==NULL_TASK) {
            return;
          }
          processed_ ++;
          out_ = new StringBuilder();
          out_.append("\n--- ")
              .append(getName())
              .append(" ---\n");
          long t0 = System.currentTimeMillis();
          if (t.process()) { success_ ++; }
          long t1 = System.currentTimeMillis();
          out_.append("--- /")
              .append(getName())
              .append(" --- ")
              .append(Long.toString(t1-t0))
              .append(" msec\n");
        }
        catch (InterruptedException iex) {
          // dispatch container requested thread to stop: exit
          out_.append("!!! Interrupted");
          return;
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
        finally {
          if (out_!=null) {
            flushOut(out_.toString());
          }
          if (t==NULL_TASK) {
            flushOut("["+getName()+"] END\n");
          }
        }
      }
    }
  }
}

