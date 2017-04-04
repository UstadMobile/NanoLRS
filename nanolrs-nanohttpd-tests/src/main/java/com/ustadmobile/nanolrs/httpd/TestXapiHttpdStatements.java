package com.ustadmobile.nanolrs.httpd;

import com.ustadmobile.nanolrs.core.NanoLRSCoreTest;
import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;
import com.ustadmobile.nanolrs.core.util.NanoLrsPlatformTestUtil;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;

import org.junit.Assert;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

/**
 * Created by mike on 2/7/17.
 */

public abstract class TestXapiHttpdStatements extends NanoLRSCoreTest {

    protected NanoLrsHttpd httpd;

    public static final int TESTPORT = 8071;

    protected String xapiUrl;

    /**
     * Override to set the persistence manager if needed
     */
    public void setPersistenceManager() {

    }

    @Before
    public void setUp() throws Exception {
        setPersistenceManager();
        httpd = new NanoLrsHttpd(TESTPORT, NanoLrsPlatformTestUtil.getContext());
        httpd.start();
        httpd.mapXapiEndpoints("/xapi");
        xapiUrl = "http://localhost:" + TESTPORT + "/xapi/";
    }

    @After
    public void tearDown() throws Exception {
        httpd.stop();
        try { Thread.sleep(500); }
        catch(InterruptedException e){}
    }

    @Test
    public void testPutStatement() throws Exception {
        InputStream stmtIn = getClass().getResourceAsStream("/com/ustadmobile/nanolrs/core/xapi-statement-page-experienced.json");
        String stmtStr = LrsIoUtils.inputStreamToString(stmtIn);

        HttpLrs lrs = new HttpLrs(xapiUrl);
        HttpLrs.LrsResponse response = lrs.putStatement(new JSONObject(stmtStr), "username", "password");
        Assert.assertEquals(response.getStatus(), 204);
    }



}
