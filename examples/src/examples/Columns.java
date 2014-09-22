package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

public class Columns
{
  public static void main(String[] args)
  {
    Input in_cols1 = new InputFile(INPUT_DIR, "columns1.in", "col_A");
    Input in_cols2 = new InputFile(INPUT_DIR, "columns1.in", "col_a", "col_1");

    Input in_all = new InputColumns(in_cols1, in_cols2);

    Output out_all = new OutputFile(RESULTS_DIR+"out.columns.gz",
        OutputFile.Flag.REPLACE, "col_A", "col_a", "col_1");

    new Pusher().debug()
        .always(out_all)
        .push(in_all);
  }
}

