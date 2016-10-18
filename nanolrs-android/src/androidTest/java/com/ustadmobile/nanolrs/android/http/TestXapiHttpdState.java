package com.ustadmobile.nanolrs.android.http;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by mike on 10/9/16.
 */

public class TestXapiHttpdState {

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
    public void testPutState() throws Exception {
        JSONObject stateObj = new JSONObject();
        stateObj.put("savekey", "saveval");

        //put it
        Context context = InstrumentationRegistry.getContext();
        HttpLrs lrs = new HttpLrs(xapiUrl);
        JSONObject agentObj = new JSONObject();
        agentObj.put("mbox", "mailto:mike@ustadmobile.com");
        byte[] jsonData = stateObj.toString().getBytes("UTF-8");
        HttpLrs.LrsResponse response = lrs.saveState("put", "username", "password", "http://www.ustadmobile.com/test/activity-state-id",
            agentObj.toString(), null, "test_state_id", "application/json", jsonData);
        Assert.assertEquals(204, response.getStatus());


        //now try and get that state back
        HttpLrs.LrsResponse getResponse = lrs.loadState("username", "password",
                "http://www.ustadmobile.com/test/activity-state-id", agentObj.toString(), null,
                "test_state_id");
        String responseStr = new String(getResponse.getServerResponse(), "UTF-8");
        Assert.assertTrue(Arrays.equals(jsonData, getResponse.getServerResponse()));
    }



}
