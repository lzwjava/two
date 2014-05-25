package com.lzw.chat.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by lzw on 14-5-25.
 */
public class TimeUtils {
  public static String getDate(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);

    String year = String.valueOf(c.get(Calendar.YEAR));
    String month = String.valueOf(c.get(Calendar.MONTH));
    String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
    String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
    String mins = String.valueOf(c.get(Calendar.MINUTE));

    StringBuffer sbBuffer = new StringBuffer();
    sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":" + mins);

    return sbBuffer.toString();
  }
}
