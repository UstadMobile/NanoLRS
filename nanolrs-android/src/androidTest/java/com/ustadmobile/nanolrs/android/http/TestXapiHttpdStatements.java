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

        /*
        URL getStmtURL = new URL(xapiUrl + "statements");
        HttpURLConnection con = (HttpURLConnection)getStmtURL.openConnection();
        int status = con.getResponseCode();
        StringWriter writer2 = new StringWriter();
        InputStream getIn = con.getInputStream();
        IOUtils.copy(getIn, writer2, "UTF-8");
        getIn.close();
        con.disconnect();
        String serverSays= writer2.toString();

        con = (HttpURLConnection)getStmtURL.openConnection();
        con.setRequestMethod("PUT");
        writer2 = new StringWriter();
        status = con.getResponseCode();
        getIn = con.getInputStream();
        IOUtils.copy(getIn, writer2, "UTF-8");
        getIn.close();
        con.disconnect();

        StringWriter writer = new StringWriter();
        IOUtils.copy(stmtIn, writer, "UTF-8");
        JSONObject stmtObj = new JSONObject(writer.toString());
        String uuid = UUID.randomUUID().toString();
        stmtObj.put("id", uuid);

        con = (HttpURLConnection)getStmtURL.openConnection();
        con.setRequestMethod("PUT");
        byte[] payload = stmtObj.toString().getBytes("UTF-8");
        con.setFixedLengthStreamingMode(payload.length);
        con.setDoOutput(true);
        OutputStream conOut = con.getOutputStream();
        conOut.write(payload);
        conOut.flush();
        conOut.close();
        status = con.getResponseCode();
        */






    }

}
