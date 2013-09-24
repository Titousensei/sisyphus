package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * This example show how to implement a simple line counter as a column Modifier.
 *
 * Modifiers compute the value of column, which will be available to other
 * actions or outputs of the same row. In this example, we add the computed
 * value  as the first column of the output.
 *
 * @author Eric Gaudet
 */
public class LineNumber
extends Modifier
{
  /**
   * This internal counter counts the line numbers so far
   */
  protected int line_number_;

  /**
   * Constructor only needs the name of the column where to write the line number
   */
  public LineNumber(String outcol)
  {
    super(new String[] { outcol });
    line_number_ = 0;
  }

  /**
   * This is where the computation done by the Modifier occurs.
   * This particular modifier does not use any input columns.
   */
  @Override
  public void compute(String[] input, String[] result)
  {
    // first do the necessary computation
    ++ line_number_;

    // finally, write the result into the output column
    result[0] = Integer.toString(line_number_);
  }
}

