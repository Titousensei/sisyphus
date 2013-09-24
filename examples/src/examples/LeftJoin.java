package examples;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

public class LeftJoin
{
  public static void main(String[] args)
  {
    String[] join_schema = new String[] { "id" };

    String[] test1_schema = new String[] { "id", "a1", "b1" };
    String[] test2_schema = new String[] { "id", "a2" };
    String[] test3_schema = new String[] { "a3", "id" };

    Input sorted1 = new InputMergeSorted(INPUT_DIR, "sorted.test1.*", join_schema, test1_schema);
    Input sorted2 = new InputMergeSorted(INPUT_DIR, "sorted.test2.*", join_schema, test2_schema);
    Input sorted3 = new InputMergeSorted(INPUT_DIR, "sorted.test3.*", join_schema, test3_schema);

    Input in_join = new InputLeftJoinSorted(join_schema, sorted1, sorted2, sorted3);

    Output out_join = new OutputFile(RESULTS_DIR+"out.leftjoin.gz", OutputFile.Flag.REPLACE, "id", "a1", "a2", "a3");
    new Pusher().debug()
        .always(out_join)
        .push(in_join);
  }
}

