package examples;

import com.thefind.sisyphus.*;

import com.thefind.sisyphus.math.StatisticsOneColumn;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class AverageRate
{
  private static final String TMP_RATE_VALUE = "rate value";

  public static void main(String[] args)
  {
    Input in_file   = new InputFile(DEFAULT_INPUT, COLUMNS);
    StatisticsOneColumn stats = new StatisticsOneColumn("avg rate", TMP_RATE_VALUE);

    new Pusher().debug()
        .always(new ColumnSubstring(TMP_RATE_VALUE, 0, -1, COL_LOAN_INTEREST_RATE))
        .always(stats)
        .push(in_file);

    double avg = stats.getAverage();
    System.out.println("\nAverage Loan rate: "+String.format("%.2f%%", avg));
  }
}

