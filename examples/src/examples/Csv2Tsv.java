package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * This is an "Hello-world" example demonstrating Sisyphus:
 * read each line from a file, and write it in another file.
 *
 * The only twist is that the input file is in comma-separated-values format,
 * and the output file will be the default gzipped tab-separated-values.
 *
 * @author Eric Gaudet
 */
public class Csv2Tsv
{
  // all the columns of the input files must be declared
  private static final String[] IN_COLUMNS = new String[] {
    "Unique Identifier", "Deal Number", "Decision", "Decision Date",
    "Effective Date", "Expiration Date", "Brokered", "Country", "Program",
    "Policy Type", "Decision Authority", "Product Description", "Term",
    "Primary Applicant", "Primary Lender", "Primary Exporter",
    "Primary Supplier", "Primary Borrower", "Primary Buyer",
    "Primary Source of Repayment", "Working Capital Delegaed Authority",
    "Approved/Declined Amount", "Disbursed/Shipped Amount",
    "Undisbursed Exposure Amount", "Outstanding Exposure Amount",
    "Loan Interest Rate", "First Claim Payment Date", "Claim Paid Amount",
    "Small Business Authorized Amount", "Woman Owned Authorized Amount",
    "Minority Owned Authorized Amount", "Fiscal Year"
  };

  public static void main(String[] args)
  {
    // Input file is in CSV format: we declare the filename and the columns
    Input in_file   = new InputFileCsv("data/Export-Import_FY_2010_Applications.csv", IN_COLUMNS)
                      .skipHeader(1); // input file's 1st line is a header we want to skip

    // Input file is in the default TSV format: we declare the filename and the columns
    Output out_file = new OutputFile(RESULTS_DIR+"Export-Import_FY_2010_Applications.tsv.gz",
        OutputFile.Flag.REPLACE, // if the file already exists, replace it with this new one
        COLUMNS);

    // Pushed is the processing class of Sisyphus
    Pusher p = new Pusher();

    // Optional: will print the content of the row when the progress marker is printed
    p.debug(-1);

    // Tell the pusher all the processing declarations before sending the file

    p.always(out_file); // 1. each row will be passed to the output file

    // That's it for the processing declarations

    // Now go ahead and process the input file
    p.push(in_file);
  }
}

