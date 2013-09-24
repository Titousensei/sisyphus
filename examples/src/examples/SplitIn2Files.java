package examples;

import com.thefind.sisyphus.*;
import com.thefind.sisyphus.test.IfEquals;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class SplitIn2Files
{
  public static void main(String[] args)
  {
    Input in_file    = new InputFile(DEFAULT_INPUT, COLUMNS);
    Output out_file1 = new OutputFile(RESULTS_DIR+"Brokered-Y.gz",
                                     OutputFile.Flag.REPLACE, COLUMNS);
    Output out_file2 = new OutputFile(RESULTS_DIR+"Brokered-N.gz",
                                     OutputFile.Flag.REPLACE, COLUMNS);

    new Pusher().debug(0)
        .onlyIf(new IfEquals(COL_BROKERED, "Y"), out_file1)
        .onlyIf(new IfEquals(COL_BROKERED, "N"), out_file2)
        .push(in_file);
  }
}

