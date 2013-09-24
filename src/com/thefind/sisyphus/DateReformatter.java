package com.thefind.sisyphus;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;

/**
 * @author Eric Gaudet
 */
public class DateReformatter
extends Modifier
{
  protected final DateFormat indate_;
  protected final DateFormat outdate_;

  protected final DateType intype_;
  protected final DateType outtype_;

  public static enum DateType { STRINGFORMAT, UNIXTIME, UNIXTIME_MILLIS };

  public DateReformatter(String outcol, DateFormat outformat, String incol, DateFormat informat)
  {
    super(new String[] { incol }, new String[] { outcol });

    indate_  = informat;
    outdate_ = outformat;

    intype_  = DateType.STRINGFORMAT;
    outtype_ = DateType.STRINGFORMAT;
  }

  public DateReformatter(String outcol, DateType outformat, String incol, DateFormat informat)
  {
    super(new String[] { incol }, new String[] { outcol });

    indate_  = informat;
    outdate_ = null;

    intype_  = DateType.STRINGFORMAT;
    outtype_ = outformat;
  }

  public DateReformatter(String outcol, DateFormat outformat, String incol, DateType informat)
  {
    super(new String[] { incol }, new String[] { outcol });

    indate_  = null;
    outdate_ = outformat;

    intype_  = informat;
    outtype_ = DateType.STRINGFORMAT;
  }

  public void compute(String[] input, String[] result)
  {
    if (input[0]!=null && !input[0].trim().isEmpty()) {
      Date dt;
      switch (intype_) {
      case STRINGFORMAT:
        try {
          dt = indate_.parse(input[0]);
        }
        catch (ParseException pex) {
          num_warnings_ ++;
          return;
        }
        break;
      case UNIXTIME:
        try {
          Integer i = Integer.parseInt(input[0]);
          dt = new Date(1000L*i);
        }
        catch (NumberFormatException pex) {
          num_warnings_ ++;
          return;
        }
        break;
      case UNIXTIME_MILLIS:
        try {
          long l = Long.parseLong(input[0]);
          dt = new Date(l);
        }
        catch (NumberFormatException pex) {
          num_warnings_ ++;
          return;
        }
        break;
      default:
        return;
      }

      switch (outtype_) {
      case STRINGFORMAT:
        result[0] = outdate_.format(dt);
        break;
      case UNIXTIME:
        result[0] = String.valueOf(dt.getTime()/1000L);
        break;
      case UNIXTIME_MILLIS:
        result[0] = String.valueOf(dt.getTime());
        break;
      default:
        return;
      }
    }
  }
}

