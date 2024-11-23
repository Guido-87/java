package com.interbanking.interbanking.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static Date getDateAMonthBefore() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }
}
