package com.ustadmobile.nanolrs.core.util;

import java.util.Calendar;

/**
 * Created by mike on 9/13/16.
 */
public class ParseUtil {

    /**
     * Format an ISO 8601 Duration from the number of milliseconds
     *
     * @param duration Duration time in MS
     *
     * @return A string formatted according to ISO8601 Duration e.g. P2H1M15S
     */
    public static String format8601Duration(long duration) {
        int msPerHour = (1000*60*60);
        int hours = (int)Math.floor(duration/msPerHour);
        long durationRemaining = duration % msPerHour;

        int msPerMin = (60*1000);
        int mins = (int)Math.floor(durationRemaining/msPerMin);
        durationRemaining = durationRemaining % msPerMin;

        int msPerS = 1000;
        int secs = (int)Math.floor(durationRemaining / msPerS);

        String retVal = "PT" + hours +"H" + mins + "M" + secs + "S";
        return retVal;
    }

    /**
     * Parse the ISO 8601 combined date and time format string
     *
     * e.g.
     * 2016-04-18T17:08:07.563789+00:00
     *
     */
    public static Calendar parse8601Timestamp(String timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(timestamp.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(timestamp.substring(5, 7)));
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timestamp.substring(8, 10)));

        if(timestamp.length() < 12) {
            return cal;
        }

        //There is a time section
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timestamp.substring(11, 13)));
        cal.set(Calendar.MINUTE, Integer.parseInt(timestamp.substring(14, 16)));
        cal.set(Calendar.SECOND, Integer.parseInt(timestamp.substring(17, 19)));

        return cal;
    }

}
