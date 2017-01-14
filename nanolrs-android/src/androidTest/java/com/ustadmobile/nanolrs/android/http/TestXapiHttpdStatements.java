package com.ustadmobile.nanolrs.android.http;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Created by mike on 9/28/16.
 */

public class TestXapiHttpdStatements {

    private NanoLrsHttpd httpd;

    public static final int TESTPORT = 8071;

    private String xapiUrl;

    @Before
    public void setUp() throws Exception {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
        httpd = new NanoLrsHttpd(TESTPORT, InstrumentationRegistry.getContext());
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
        Context context = InstrumentationRegistry.getContext();
        InputStream stmtIn = context.getAssets().open("xapi-statement-page-experienced.json");
        StringWriter strWriter = new StringWriter();
        IOUtils.copy(stmtIn, strWriter, "UTF-8");
        String stmtStr = strWriter.toString();

        HttpLrs lrs = new HttpLrs(xapiUrl);
        HttpLrs.LrsResponse response = lrs.putStatement(new JSONObject(stmtStr), "username", "password");
        Assert.assertEquals(response.getStatus(), 204);
    }

}
