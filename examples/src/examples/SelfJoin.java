package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

public class SelfJoin
{
  public static void main(String[] args)
  {
    Input sorted1 = new InputFile(INPUT_DIR+"selfjoin.sorted.in", "id", "a", "b");
    Input in_join = new InputSelfJoinSorted(sorted1, "id");

    Output out_join = new OutputFile(RESULTS_DIR+"out.selfjoin.gz", OutputFile.Flag.REPLACE,
        "id", "a", "r.b");

    new Pusher().debug()
        .always(out_join)
        .push(in_join);
  }
}

