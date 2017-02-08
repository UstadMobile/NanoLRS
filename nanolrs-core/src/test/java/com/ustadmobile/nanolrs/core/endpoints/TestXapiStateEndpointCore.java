package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.NanoLRSCoreTest;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.NanoLrsPlatformTestUtil;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by mike on 2/6/17.
 */

public abstract class TestXapiStateEndpointCore extends NanoLRSCoreTest {

    @Test
    public void testStateEndpoint() throws Exception {
        JSONObject stateDoc = new JSONObject();
        stateDoc.put("key1", "value1");
        Object context = NanoLrsPlatformTestUtil.getContext();
        byte[] stateContent = stateDoc.toString().getBytes("UTF-8");

        JSONObject actorObj = new JSONObject();
        String mbox = "mailto:testuser@ustadmobile.com";
        actorObj.put("mbox", mbox);


        String activityId = "http://www.ustadmobile.com/test-state-activity-id";
        String stateId = "unit_test_id";

        XapiState state = XapiStateEndpoint.createOrUpdateState(context, "put", "application/json",
                activityId, actorObj.toString(), null, stateId, stateContent);

        Assert.assertNotNull("State object created by createOrUpdateState endpoint", state);


        //dig it out again
        XapiState retrieved = PersistenceManager.getInstance().getStateManager().findByActivityAndAgent(
                context, activityId, mbox, null, null, null, stateId);
        Assert.assertNotNull("State object created can be retrieved by parameters", retrieved);

        XapiState otherIdState = PersistenceManager.getInstance().getStateManager().findByActivityAndAgent(
                context, activityId, mbox, null, null, null, stateId+"_otherid");
        Assert.assertNull("State object not matched when stateId is different", otherIdState);

        XapiState otherAgent = PersistenceManager.getInstance().getStateManager().findByActivityAndAgent(
                context, activityId, "mailto:someone@domain.com", null, null, null, stateId+"_otherid");

        Assert.assertNull("State not matched when searching by another agent", otherAgent);

    }
}
