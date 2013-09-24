package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class SplitToMultipleFiles
{
  public static void main(String[] args)
  {
    Input in_file    = new InputFile(DEFAULT_INPUT, COLUMNS);
    Output out_multi = new SplitByOneColumn(
        RESULTS_DIR, "SplitToMultipleFiles - ", COL_PROGRAM, ".gz",
        COLUMNS);

    new Pusher().debug(0)
        .always(out_multi)
        .push(in_file);
  }
}

