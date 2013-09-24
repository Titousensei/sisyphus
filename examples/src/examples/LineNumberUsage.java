package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * This example show how we use the simple modifier LineNumber to add as the line
 * number as the first column of the input.
 *
 * @author Eric Gaudet
 */
public class LineNumberUsage
{
  private static final String COL_LINE_NUMBER = "Line Number";

  private static final String[] OUT_COLUMNS = new String[] {
    COL_LINE_NUMBER,
    "Unique Identifier",
    "Deal Number",
    "Decision",
    "Decision Date",
    "Effective Date",
    "Expiration Date",
    "Brokered",
    "Country",
    "Program",
    "Policy Type",
    "Term",
    "Loan Interest Rate",
    "First Claim Payment Date",
    "Fiscal Year"
  };

  public static void main(String[] args)
  {
    Input in_file   = new InputFile(DEFAULT_INPUT, COLUMNS);
    Output out_file = new OutputFile(RESULTS_DIR+"NumberedLines.gz", OutputFile.Flag.REPLACE, OUT_COLUMNS);

    new Pusher().debug(-1)
        .always(new LineNumber(COL_LINE_NUMBER))
        .always(out_file)
        .push(in_file);
  }
}

