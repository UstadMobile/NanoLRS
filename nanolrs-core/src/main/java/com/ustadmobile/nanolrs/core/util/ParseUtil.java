package com.ustadmobile.nanolrs.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by mike on 9/13/16.
 */
public class ParseUtil {

    public static final int MS_PER_HOUR = 3600000;//60mins *60seconds * 1000 milliseconds

    public static final int MS_PER_MIN = 60000;//60seconds * 1000 milliseconds

    public static final char[] TZ_START_CHARS = new char[]{'+', '-', '−'};

    public static final int TZ_START_CHARS_PLUS = 0;

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

    private static String pad0(int num) {
        if(num < 10) {
            return "0" + num;
        }else {
            return String.valueOf(num);
        }
    }

    /**
     * For the given date object return a correctly formatted ISO 8601 compliant
     * String.  If the calendar includes a time zone offset it will be adjusted to UTC+0
     * as recommended by the xAPI spec
     *
     * @param cal
     */
    public static String format8601Timestamp(Calendar cal) {
        StringBuffer sb = new StringBuffer();
        long timeInMillis = cal.getTimeInMillis();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        cal.setTimeInMillis(timeInMillis - zoneOffset);
        cal.set(Calendar.ZONE_OFFSET, 0);


        cal.getTime();//Force recompute

        sb.append(cal.get(Calendar.YEAR)).append('-');
        sb.append(pad0(cal.get(Calendar.MONTH) + 1)).append('-');
        sb.append(pad0(cal.get(Calendar.DAY_OF_MONTH))).append('T');
        sb.append(pad0(cal.get(Calendar.HOUR_OF_DAY))).append(':');
        sb.append(pad0(cal.get(Calendar.MINUTE))).append(":");
        sb.append(pad0(cal.get(Calendar.SECOND))).append("+00:00");
        return sb.toString();
    }

    /**
     * Parse the ISO 8601 combined date and time format string
     *
     * e.g.
     * 2016-04-18T17:08:07.563789+00:00
     *
     */
    public static Calendar parse8601Timestamp(String timestamp) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        cal.set(Calendar.YEAR, Integer.parseInt(timestamp.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(timestamp.substring(5, 7)) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timestamp.substring(8, 10)));

        if(timestamp.length() < 12) {
            return cal;
        }

        //There is a time section
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timestamp.substring(11, 13)));
        cal.set(Calendar.MINUTE, Integer.parseInt(timestamp.substring(14, 16)));
        cal.set(Calendar.SECOND, Integer.parseInt(timestamp.substring(17, 19)));


        if(timestamp.length() < 20 || timestamp.indexOf('Z', 19) != -1) {
            //There is no timezone, or it is marked Z for Zulu/UTC
            cal.set(Calendar.ZONE_OFFSET, 0);
            return cal;
        }

        //There is a timezone indicator - set that
        int tzStartPos = -1;
        int tzMultiplier = 1;//1 or-1 depending on symbol
        for(int i = 0; i < TZ_START_CHARS.length; i++) {
            tzStartPos = timestamp.indexOf(TZ_START_CHARS[i], 19);
            if(tzStartPos != -1) {
                tzMultiplier = i == TZ_START_CHARS_PLUS ? 1 : -1;
                break;
            }
        }

        if(tzStartPos != -1) {
            int tzHourOffset = 0;
            int tzMinOffset = 0;

            tzHourOffset = Integer.parseInt(timestamp.substring(tzStartPos+1, tzStartPos+3));


            if(timestamp.length() > tzStartPos+3) {
                int tzMinPosOffset =  timestamp.charAt(tzStartPos+4) == ':' ? 1 : 0;
                tzMinOffset = Integer.parseInt(timestamp.substring(tzStartPos + 4 +tzMinPosOffset,
                        tzStartPos + 6 + tzMinPosOffset));
            }

            cal.set(Calendar.ZONE_OFFSET,
                    ((tzHourOffset * MS_PER_HOUR) + (tzMinOffset * MS_PER_MIN)) * tzMultiplier);
        }

        return cal;
    }


}
