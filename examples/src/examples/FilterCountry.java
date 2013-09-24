package examples;

import com.thefind.sisyphus.*;
import com.thefind.sisyphus.test.IfEquals;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class FilterCountry
{
  private static final String COL_COUNTRY_TOLOWER = "Country -> toLower";

  public static void main(String[] args)
  {
    String country = args[0].toLowerCase();

    Input in_file   = new InputFile(DEFAULT_INPUT, COLUMNS);
    Output out_file = new OutputFile(RESULTS_DIR+"FilterCountry-"+country+".gz",
                                     OutputFile.Flag.REPLACE, COLUMNS);

    new Pusher().debug()
        .always(new ColumnToLower(COL_COUNTRY, COL_COUNTRY_TOLOWER))
        .onlyIf(new IfEquals(COL_COUNTRY_TOLOWER, country), out_file)
        .push(in_file);
  }
}

