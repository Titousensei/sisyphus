package examples;

import java.io.EOFException;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * InputYielder is a base class that implements a generator pattern,
 * similar to python's yield keyword. As such, the implementation of
 * this class is fully done in mainLoop instead of a more cumbersome
 * Iterator implementation.
 *
 * @author Eric Gaudet
 */
public class InputRange
extends InputYielder
{
  protected final int begin_;
  protected final int end_;
  
  public InputRange(int begin, int end, String out_counter)
  {
    super (new String[] { out_counter });
    begin_ = begin;
    end_   = end;
  }

  @Override
  protected boolean beforeLoop()
  {
    // Optional step called on open().
    // Used to initiate db connections or other resources.

    // Return whether it was successful or not.
    return true;
  }
  
  @Override
  protected void mainLoop()
  throws Exception
  {
    for (int i=begin_; i<=end_; i++) {
      // Since this is a generator pattern, the row needs to be instantiated each time.
      String[] row = new String[1];
      
      // Compute the row values
      // (in this case, it's just populate the counter)
      row[0] = String.valueOf(i);
      
      // Pass the row to the main program
      yield(row);
    }
  }
  
  @Override
  protected void afterLoop()
  {
    // Optional step called on close().
    // Used to close db connections or clean-up other resources.
  }
  
  @Override
  protected String toStringWhich()
  { return begin_ + ".." + end_; }

  public static void main(String[] args)
  {
    Input in = new InputRange(123, 168, "id");

    Output out = new OutputFile(RESULTS_DIR + "range1.out.gz", OutputFile.Flag.REPLACE,
        "id");

    new Pusher().debug()
        .always(out)
        .push(in);
  }
}
