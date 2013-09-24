package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

public class Sort
{
  public static void main(String[] args)
  {
    // Small number to demonstrate the splits.
    // Realistically, should be 1,000,000 or more.
    int batch_size = 1000;

    Input in_file = new InputFile(DEFAULT_INPUT, COLUMNS);

    String[] sort_columns = new String[] { COL_LOAN_INTEREST_RATE, COL_COUNTRY };

    Output sort = new OutputSortSplit(RESULTS_DIR+"sorted", batch_size,
                          sort_columns, COLUMNS);
    new Pusher()
        .always(sort)
        .push(in_file);
  }
}

