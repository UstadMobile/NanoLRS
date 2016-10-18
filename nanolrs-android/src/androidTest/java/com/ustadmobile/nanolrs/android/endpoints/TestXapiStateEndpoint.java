package com.ustadmobile.nanolrs.android.endpoints;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.endpoints.XapiStateEndpoint;
import com.ustadmobile.nanolrs.core.model.XapiStateProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by mike on 10/8/16.
 */

public class TestXapiStateEndpoint {

    @Before
    public void setUp() throws Exception {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
    }

    @Test
    public void testStateEndpoint() throws Exception {
        JSONObject stateDoc = new JSONObject();
        stateDoc.put("key1", "value1");
        Context context = InstrumentationRegistry.getContext();
        byte[] stateContent = stateDoc.toString().getBytes("UTF-8");

        JSONObject actorObj = new JSONObject();
        String mbox = "mailto:testuser@ustadmobile.com";
        actorObj.put("mbox", mbox);


        String activityId = "http://www.ustadmobile.com/test-state-activity-id";
        String stateId = "unit_test_id";

        XapiStateProxy state = XapiStateEndpoint.createOrUpdateState(context, "put", "application/json",
                activityId, actorObj.toString(), null, stateId, stateContent);

        Assert.assertNotNull(state);


        //dig it out again
        XapiStateProxy retrieved = PersistenceManager.getInstance().getStateManager().findByActivityAndAgent(
                context, activityId, mbox, null, null, null, stateId);
        Assert.assertNotNull(retrieved);

        XapiStateProxy otherIdState = PersistenceManager.getInstance().getStateManager().findByActivityAndAgent(
                context, activityId, mbox, null, null, null, stateId+"_otherid");
        Assert.assertNull(otherIdState);

        XapiStateProxy otherAgent = PersistenceManager.getInstance().getStateManager().findByActivityAndAgent(
                context, activityId, "mailto:someone@domain.com", null, null, null, stateId+"_otherid");

        Assert.assertNull(otherAgent);

    }



}
