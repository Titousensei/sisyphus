package com.thefind.testutil;

import java.io.*;
import java.util.*;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;

/**
 * Prints a single lines of tests to a specified Writer.
 * Inspired by the BriefJUnitResultFormatter and
 * XMLJUnitResultFormatter.
 *
 * @author Eric Gaudet
 */
public class OneLinerFormatter
implements JUnitResultFormatter
{
  private static final String SPACES = " .......................................";

  /**
  * Where to write the log to.
  */
  private OutputStream out_;

  /**
  * Used for writing the results.
  */
  private PrintWriter output_;

  //private String systemOutput_ = null;
  //private String systemError_ = null;

  /**
  * tests that failed.
  */
  private final Map<Test, String> failedTests_ = new HashMap();
  /**
  * Timing helper.
  */
  private final Map<Test, Long> testStarts_ = new HashMap();

  private int count_total_;
  private int count_fail_;
  private int count_long_;

  private JUnitTest last_suite_ = null;

  /**
  * Constructor for OneLinerFormatter.
  */
  //public OneLinerFormatter()
  //{ results_ = new StringWriter(); }

  @Override
  public void setOutput(OutputStream out)
  {
    out_ = out;
    output_ = new PrintWriter(out);
  }

  @Override
  public void setSystemOutput(String out) {}
  //{ systemOutput_ = out; }

  @Override
  public void setSystemError(String err) {}
  //{ systemError_ = err; }

  @Override
  public void startTestSuite(JUnitTest suite)
  {
    if (output_ == null || suite.runCount()>0) {
      return;
    }
    if (suite.runCount()>0) {
      output_.println();
      output_.println();
    }

    output_.println("----------------------------------------------------------");
    output_.println(suite.getName());
    output_.println();
    output_.flush();

    count_total_ = 0;
    count_fail_ = 0;
    count_long_ = 0;
  }

  @Override
  public void endTestSuite(JUnitTest suite)
  {
    if (output_ == null) {
      return;
    }

    try {
      if (count_fail_==0) {
        output_.println();
        if (count_long_>0) {
          output_.print("TOO LONG: ");
          output_.print(count_long_);
          output_.print(" / ");
          output_.print(count_total_);
        }
        else if (count_total_>0) {
          output_.print("SUCCESS: ");
          output_.print(count_total_);
        }
      }
      output_.println();
      output_.flush();
    }
    finally {
      if (out_ != System.out && out_ != System.err) {
        try {
          out_.close();
        }
        catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  @Override
  public void startTest(Test test)
  {
    testStarts_.put(test, System.currentTimeMillis());
    String methodName = JUnitVersionHelper.getTestCaseName(test);
    output_.print(methodName);
    int l = 40-methodName.length();
    if (l>=3) {
      output_.print(SPACES.substring(0, l));
    }
    else {
      output_.print(" ..");
    }
  }

  @Override
  public void endTest(Test test)
  {
    long l = 0L;
    if (testStarts_.containsKey(test)) {
      l = System.currentTimeMillis() - testStarts_.get(test);
    }

    String failure = failedTests_.get(test);

    ++ count_total_;
    if (failure!=null) {
      output_.print(" FAILED");
      ++ count_fail_;
    }
    else if (l>1000L) {
      output_.print(" TOO LONG");
      ++ count_long_;
    }
    else {
      output_.print(" OK");
    }
    output_.println(String.format(" (%3.3f sec) ", l / 1000.0));

    if (failure!=null) {
      output_.println(failure);
    }
    output_.flush();
  }

  /**
  * Interface TestListener for JUnit &gt; 3.4.
  *
  * A test failed.
  * @param test a test
  * @param t    the assertion failed by the test
  */
  @Override
  public void addFailure(Test test, AssertionFailedError t)
  { formatError("FAILED", test, (Throwable) t); }

  /**
  * A test caused an error.
  * @param test  a test
  * @param error the error thrown by the test
  */
  @Override
  public void addError(Test test, Throwable error)
  { formatError("Caused an ERROR", test, error); }

  /**
  * Format an error and print it.
  * @param type the type of error
  * @param test the test that failed
  * @param error the exception that the test threw
  */
  protected void formatError(String type,
      Test test, Throwable error)
  {
    if (test == null) {
      return;
    }

    StringBuilder sb = new StringBuilder();

    sb.append(error.getClass().getSimpleName())
      .append(": ")
      .append((error.getMessage() != null) ? error.getMessage() : error.toString())
      .append('\n');

    String testCaseClassName = JUnitVersionHelper.getTestCaseClassName(test);
    for (StackTraceElement ste : error.getStackTrace()) {
      if (ste.getClassName().equals(testCaseClassName)) {
        sb.append("    at ")
          .append(ste.getFileName())
          .append(':')
          .append(ste.getLineNumber())
          .append(" (")
          .append(ste.getMethodName())
          .append(')');
      }
    }

    failedTests_.put(test, sb.toString());
  }
}

