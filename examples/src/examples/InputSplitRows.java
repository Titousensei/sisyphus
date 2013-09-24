package examples;

import java.io.EOFException;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class InputSplitRows
extends InputCustom
{
  protected String[] current_row_;
  protected int cut1_;
  protected int cut2_;

  public InputSplitRows(String in_id, String in_comb, Input in, String out_id, String out_comb)
  { super (new String[] { in_id, in_comb }, in, new String[] { out_id, out_comb }); }

  @Override
  protected void computeRow(String[] result)
  throws EOFException
  {
    if (current_row_==null) {
      current_row_ = readInput();
      cut1_ = 0;
      while (current_row_[1]==null) {
        current_row_ = readInput();
      }
      cut2_ = current_row_[1].indexOf(',');
    }
    else {
      cut1_ = cut2_+1;
      cut2_ = current_row_[1].indexOf(',', cut1_);
    }
    result[0] = current_row_[0];
    if (cut2_>cut1_) {
      result[1] = current_row_[1].substring(cut1_, cut2_);
    }
    else {
      result[1] = current_row_[1].substring(cut1_);
      current_row_ = null;
    }
  }

  public static void main(String[] args)
  {
    Input comb1 = new InputFile(INPUT_DIR + "combined1.in", "id", "dummy", "comb");
    Input in = new InputSplitRows("id", "comb", comb1, "id", "c1");

    Output out = new OutputFile(RESULTS_DIR + "combined1.out.gz", OutputFile.Flag.REPLACE,
        "id", "c1");

    new Pusher().debug()
        .always(out)
        .push(in);
  }
}
