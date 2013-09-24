package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class NewUpdateDelete
{
  private static final String H_UID  = "#uid";
  private static final String H_DATA = "#data";

  // This hash is needed to transform the string in "Unique Identifier"
  // to a 64-bits value used as a look-up key
  private static final Modifier UID_TO_HASH = new ColumnsHashLong(H_UID, COL_UID);

  // This hash represents all the columns: if anything changes,
  // this hash will change too.
  private static final Modifier DATA_TO_HASH = new ColumnsHashLong(H_DATA, COLUMNS);

  public static void main(String[] args)
  throws LoadingException
  {
    // Normally this is copied from the previous day, but in this demo
    // we need to generate the current state
    Input in_prepare = new InputFile(DEFAULT_INPUT, COLUMNS);
    KeyBinding bind_prepare = new KeyBinding(H_UID, H_DATA);
    new Pusher()
        .always(UID_TO_HASH)
        .always(DATA_TO_HASH)
        .always(new OutputKeyBinding(bind_prepare))
        .push(in_prepare);

    bind_prepare.save(INPUT_DIR+"_bind.current_data");

    // Now we do the normal processing: we generate the current state

    Input in_today = new InputFileGroup(INPUT_DIR, "data_*.gz", COLUMNS);

    KeyBinding bind_yesterday = KeyBinding.load(INPUT_DIR+"_bind.current_data");
    KeyBinding bind_today = new KeyBinding(H_UID, H_DATA);

    Output out_new     = new OutputFile(RESULTS_DIR+"data_new.gz",
                                OutputFile.Flag.REPLACE, COLUMNS);
    Output out_changed = new OutputFile(RESULTS_DIR+"data_changed.gz",
                                OutputFile.Flag.REPLACE, COLUMNS);
    Output out_same    = new OutputFile(RESULTS_DIR+"data_same.gz",
                                OutputFile.Flag.REPLACE, COLUMNS);

    new Pusher()
        .always(UID_TO_HASH)
        .always(DATA_TO_HASH)
        .always(new OutputKeyBinding(bind_today))
        .ifMiss( bind_yesterday, new BreakAfter(out_new))
        .ifDiff( bind_yesterday, new BreakAfter(out_changed))
        .ifMatch(bind_yesterday, new BreakAfter(out_same))
        .push(in_today);

    bind_today.save(RESULTS_DIR+"_bind.current_data");
  }
}

