package com.ustadmobile.nanolrs.test.core.util;

import com.ustadmobile.nanolrs.core.util.ParseUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;

/**
 * Created by mike on 10/5/16.
 */
public abstract class TestParseUtils {

    @Test
    public void test8601Timestamp() {
        Calendar now = Calendar.getInstance();
        String timestampNow = ParseUtil.format8601Timestamp(now);
        Calendar parsed = ParseUtil.parse8601Timestamp(timestampNow);

        long diff = Math.abs(now.getTimeInMillis() - parsed.getTimeInMillis());
        Assert.assertTrue("Tiemstamp formatted and reparsed: difference less than 1 second",
                diff < 1000);

        String[][] testStamps = new String[][] {
            {"2015-12-18T12:17:00+00:00", "2015-12-18T12:17:00+00:00"},
            {"2015-12-18T14:17:00+02:00", "2015-12-18T12:17:00+00:00"},
            {"2015-12-18T12:17:00Z", "2015-12-18T12:17:00+00:00"},
            {"2015-12-18T10:17:00-02:00", "2015-12-18T12:17:00+00:00"},
            {"2015-12-18T10:17:00−02:00", "2015-12-18T12:17:00+00:00"}//unicode minus sign
        };

        Calendar parsedCal;
        String parsedCalTimestamp;
        for(int i = 0; i < testStamps.length; i++) {
            parsedCal = ParseUtil.parse8601Timestamp(testStamps[i][0]);
            parsedCalTimestamp = ParseUtil.format8601Timestamp(parsedCal);
            Assert.assertEquals("Timestamp " + testStamps[i] +
                ": parsed to cal and back to string as expected", testStamps[i][1],
                parsedCalTimestamp);
        }
    }

}
