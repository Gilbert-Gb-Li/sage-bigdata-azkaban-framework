package com.haima.sage.bigdata.azkaban.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author liuyang
 */
public class DateFormatUtil {

    private static final String REGEX = "^\\d{4}(\\-)\\d{2}(\\-)\\d{2}$";
    public static final String YYYYMMDD = "yyyy-MM-dd";
    public static final String YYYYMMDDHH = "yyyy-MM-dd-HH";

    public static Date parse(String dateStr, String pattern) throws ParseException {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.parse(dateStr);
    }

    public static boolean checkDate(String dateStr) {
        return dateStr.matches(REGEX);
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern(pattern);
        return df.format(date);
    }

    public static String getDateStr(Date date, String pattern, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(calendar.getTime());
    }

    public static String getHourStr(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, amount);
        DateFormat df = new SimpleDateFormat(YYYYMMDDHH);
        String dateStr = df.format(calendar.getTime());
        String [] temp = dateStr.split("-");
        return temp[3];
    }

}
