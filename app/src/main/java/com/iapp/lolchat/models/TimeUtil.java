package com.iapp.lolchat.models;

import android.content.Context;
import android.text.format.DateFormat;
import com.iapp.lolchat.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author IgorIvanov
 * @version 1.0
 * Utility class for date processing
 * */
public final class TimeUtil {

    /**
     * Method for converting a date from GMT to time zone
     * of a given PC and turning it into a readable from
     * @param context from which the method is invoked
     * @param time is the time in GMT time zone
     * @return readable date in current time zone
     * */
    public static String defineTimeView(Context context, long time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(new Date(time));
        calendar.setTimeZone(TimeZone.getDefault());

        long passed = new Date().getTime() - calendar.getTime().getTime();

        String res;
        if (TimeUtil.getSeconds(passed) < 60) {
            // TODO
            res = "меньше минуты";
        } else if (TimeUtil.getMinutes(passed) < 60) {
            // TDOD
            res = DateFormat.format("mm ", new Date(passed)) + "минут назад";
        } else if (TimeUtil.getHours(passed) < 24){
            res = String.valueOf(DateFormat.format("HH:mm", new Date(time)));
        } else if (TimeUtil.getDays(passed) < 365) {
            res = String.valueOf(DateFormat.format("dd MMMM", new Date(time)));
        } else {
            res = String.valueOf(DateFormat.format("dd MMMM yyyy", new Date(time)));
        }

        return res;
    }

    /**
     * Method for converting time from milliseconds to seconds
     * @param time is milliseconds
     * @return seconds
     * */
    public static int getSeconds(long time) {
        return Math.round(time / 1000);
    }

    /**
     * Method for converting time from milliseconds to minutes
     * @param time is milliseconds
     * @return minutes
     * */
    public static int getMinutes(long time) {
        return Math.round(getSeconds(time) / 60);
    }

    /**
     * Method for converting time from milliseconds to hours
     * @param time is milliseconds
     * @return hours
     * */
    public static int getHours(long time) {
        return Math.round(getMinutes(time) / 60);
    }

    /**
     * Method for converting time from milliseconds to days
     * @param time is milliseconds
     * @return days
     * */
    public static int getDays(long time) {
        return Math.round(getHours(time) / 24);
    }

    /**
     * Method for converting time from milliseconds to years
     * @param time is milliseconds
     * @return years
     * */
    public static int getYears(long time) {
        return Math.round(getDays(time) / 365);
    }
}
