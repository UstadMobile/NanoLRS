package com.ustadmobile.nanolrs.httpd;

import com.ustadmobile.nanolrs.core.NanoLRSCoreTest;
import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by mike on 2/6/17.
 */

public abstract class TestXapiHttpdStateHttp extends NanoLRSCoreTest {

    protected NanoLrsHttpd httpd;

    public static final int TESTPORT = 8071;

    protected String xapiUrl;

    @Before
    public void setUp() throws Exception {
        setPersistenceManager();
        httpd = new NanoLrsHttpd(TESTPORT, getContext());
        httpd.start();
        httpd.mapXapiEndpoints("/xapi");
        xapiUrl = "http://localhost:" + TESTPORT + "/xapi/";
    }

    //if required in the subclass : override this - e.g. for Android
    protected void setPersistenceManager() {

    }

    @Test
    public void testPutState() throws Exception {
        JSONObject stateObj = new JSONObject();
        stateObj.put("savekey", "saveval");

        //put it
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

    @After
    public void tearDown() throws Exception {
        httpd.stop();
        try { Thread.sleep(500); }
        catch(InterruptedException e){}
    }


}
