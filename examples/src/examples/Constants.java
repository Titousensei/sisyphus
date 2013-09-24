package examples;

/**
 * @author Eric Gaudet
 */
public class Constants
{
  public static final String COL_UID     = "Unique Identifier";

  public static final String COL_COUNTRY = "Country";

  public static final String COL_PROGRAM = "Program";
  //  232 Guarantee
  // 2899 Insurance
  //   50 Loan
  //  562 Working Capital

  public static final String COL_BROKERED = "Brokered"; // Y/N

  public static final String COL_TERM     = "Term";
  //    144 Long Term
  //  293 Medium Term
  // 3306 Short Term

  public static final String COL_LOAN_INTEREST_RATE = "Loan Interest Rate";

  public static final String[] COLUMNS = new String[] {
                                    // Cardinality
    COL_UID,                        // 3743
    "Deal Number",                  // 3479
    "Decision",                     // 2
    "Decision Date",                // 277
    "Effective Date",               // 264
    "Expiration Date",              // 467
    COL_BROKERED,                   // 2
    COL_COUNTRY,                    // 92
    COL_PROGRAM,                    // 4
    "Policy Type",                  // 13
    "Term",                         // 3
    COL_LOAN_INTEREST_RATE,         // 27
    "First Claim Payment Date",     // 101
    "Fiscal Year"                   // 1
  };

  public static final String INPUT_DIR     = "data/";
  public static final String RESULTS_DIR   = "results/";
  public static final String DEFAULT_INPUT = INPUT_DIR + "Export-Import_FY_2010_Applications.gz";
}

