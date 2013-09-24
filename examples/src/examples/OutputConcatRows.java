package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class OutputConcatRows
extends OutputCustom
{
  protected String id_;
  protected StringBuilder comb_;

  public OutputConcatRows(String in_id, String in_comb, Output out, String out_id, String out_comb)
  { super (new String[] { in_id, in_comb }, out, new String[] { out_id, out_comb }); }

  @Override
  protected void append(String[] in_row)
  {
    if (id_!=null && id_.equals(in_row[0])) {
      comb_.append(',');
    }
    else {
      if (comb_!=null) {
        flush();
      }
      id_ = in_row[0];
      comb_ = new StringBuilder();
    }
    if (in_row[1]!=null) {
      comb_.append(in_row[1]);
    }
  }

  @Override
  protected void flush()
  { writeRow(id_, comb_.toString()); }

  public static void main(String[] args)
  {
    Input in = new InputFile(INPUT_DIR + "combined2.in", "id", "dummy1", "c1", "dummy2");

    Output comb2 = new OutputFile(RESULTS_DIR + "combined2.out.gz", OutputFile.Flag.REPLACE, "id", "comb");
    Output out = new OutputConcatRows("id", "c1", comb2, "id", "comb");

    new Pusher().debug()
        .always(out)
        .push(in);
  }
}

