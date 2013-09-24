package com.thefind.util;

import java.io.*;
import java.util.*;

/**
 * Class to run background shell processes in a different thread.
 *
 * Typical use:
 * <pre>
 *   OutputStream out = new ByteArrayOutputStream();
 *   BackgroundShell bgsh = new BackgroundShell(out, "./run.sh")
 *   bgsh.start();
 *   // .. do stuff
 *   long wait_time = bgsh.finish();
 *   System.out.println(bgsh.toString());
 * </pre>
 *
 * @author Eric Gaudet
 */
public class BackgroundShell
extends Thread
{
  private final OutputStream out_;
  private final String[] cmd_;

  private boolean success_;
  private long start_time_;
  private long end_time_;

  /**
   * Constructor of the thread for a shell command. Stdout and stderr will be
   * printed in a container within this object.
   *
   * @param cmd the shell command and its parameters
   */
  public BackgroundShell(String... cmd)
  { this(new ByteArrayOutputStream(), cmd); }

  /**
   * Constructor of the thread for a shell command.
   *
   * @param out where to print stdout and stderr
   * @param cmd the shell command and its parameters
   */
  public BackgroundShell(OutputStream out, String... cmd)
  {
    super();
    out_ = out;
    cmd_ = cmd;

    success_ = false;
  }

  /**
   * Whether the shell command was a success or if there was an exception while running it.
   *
   * @return true if there was no exception during execution.
   */
  public boolean isSuccess() { return success_; }

  /**
   * The duration of the run.
   *
   * @return duration of the execution in milliseconds.
   */
  public long getDuration() { return (end_time_ - start_time_); }

  /**
   * Wait for the shell command to finish.
   *
   * @return duration of the wait in milliseconds.
   */
  public long finish()
  {
    long t0 = System.currentTimeMillis();
    try {
      join();
    }
    catch (InterruptedException iex) {
      Thread.currentThread().interrupt();
    }
    return (System.currentTimeMillis() - t0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    System.err.print("[BackgroundShell] STARTING "+Arrays.toString(cmd_));
    start_time_ = System.currentTimeMillis();
    success_ = runSh(out_, cmd_);
    end_time_ = System.currentTimeMillis();
    System.err.print("[BackgroundShell] DONE "+Arrays.toString(cmd_));
  }

  /**
   * The output of the shell command (both sdtout and stderr).
   */
  @Override
  public String toString()
  { return out_.toString(); }


  /**
   * Run a shell command directly. Note: If you're getting IOException about memory,
   * use jdk 1.6.0_23 or above for a better fork().<br>
   *<br>
   * @param out_raw where to print stdout and stderr
   * @param cmd the shell command and its parameters
   */
  public static boolean runSh(OutputStream out_raw, String... cmd)
  {
    PrintStream out = new PrintStream(out_raw);
    InputStream in = null;
    Date start = new Date();
    try {
      out.print("### STARTING - ");
      out.print(start.toString());
      out.print(' ');
      out.print(Arrays.toString(cmd));
      out.println();

      ProcessBuilder builder = new ProcessBuilder(cmd);
      builder.redirectErrorStream(true);
      Process p = builder.start();

      in = p.getInputStream();
      int c;
      boolean first = true;
      while ((c = in.read()) != -1) {
        if (first) {
          out.print("# ");
        }
        char k = (char) c;
        out.print(k);
        first = (k=='\n');
      }

      if (!first) { out.print('\n'); }
      Date end = new Date();
      long duration = end.getTime() - start.getTime();
      out.print("### DONE in ");
      out.print(StringUtil.readableTime(duration));
      out.print(" - ");
      out.print(end.toString());
      out.print(' ');
      out.print(Arrays.toString(cmd));
      out.println();
      return true;
    }
    catch(SecurityException secex) {
      secex.printStackTrace();
    }
    catch(IndexOutOfBoundsException ioobex) {
      System.err.println("[IOUtils.runSh] WARNING - No command to run");
    }
    catch(IOException ioex) {
      System.err.println("[IOUtils.runSh] ERROR - Can't run command "+Arrays.toString(cmd)+": "+ioex.toString());
    }
    finally {
      out.flush();
    }

    return false;
  }
}

