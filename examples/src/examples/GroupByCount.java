package examples;

import java.util.*;

import com.thefind.sisyphus.*;

import static examples.Constants.*;

/**
 * @author Eric Gaudet
 */
public class GroupByCount
{
  public static final String COL_COUNTRY_HASH = "#Country";
  public static final String COL_COUNT        = "Count by Country";

  public static void main(String[] args)
  {
    Input in_file   = new InputFile(DEFAULT_INPUT, COLUMNS);

    KeyMap groupby_count = new KeyMap(COL_COUNTRY_HASH, COL_COUNT);
    Map<String, String> country_names = new HashMap();

    // First pass: count each country
    new Pusher().debug(0)
        .always(new ColumnsHashLong(COL_COUNTRY_HASH, COL_COUNTRY))
        .always(new HashMapSetter(country_names, COL_COUNTRY_HASH, COL_COUNTRY))
        .always(new KeyMapIncrement(groupby_count, 1))
        .push(in_file);

    // Second pass: print the results with the full country name, not the hash value
    new Pusher().debug()
        .always(new HashMapGetter(country_names, COL_COUNTRY_HASH, COL_COUNTRY_HASH))
        .push(new InputKeyMap(groupby_count));
  }
}

