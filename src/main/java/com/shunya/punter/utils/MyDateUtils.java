package com.shunya.punter.utils;

import java.util.Calendar;
import java.util.Date;

public class MyDateUtils {

    public Date addBusinessDays(Date date, int numberOfDays) {
        int count = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        while (count < numberOfDays) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                count++;
        }
        return calendar.getTime();
    }
}
