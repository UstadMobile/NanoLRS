package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.NanoLRSCoreTest;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * Created by mike on 1/21/17.
 */

public abstract class TestXapiActivityEndpointCore extends NanoLRSCoreTest{

    @Test
    public void testActivityEndpoint() throws Exception {
        InputStream activityIn = getClass().getResourceAsStream("/com/ustadmobile/nanolrs/core/test-activity.json");
        String activityStr = readInputStream(activityIn);

        activityIn.close();

        JSONObject activityObj = new JSONObject(activityStr);
        XapiActivity activity = XapiActivityEndpoint.createOrUpdate(getContext(), activityObj);
        Assert.assertNotNull(activity);

        //now we should be able to find it by ID
        XapiActivityManager manager = PersistenceManager.getInstance().getActivityManager();
        XapiActivity found = manager.findByActivityId(getContext(),
                activityObj.getString("id"));
        Assert.assertNotNull(found);

        //now delete it
        manager.deleteByActivityId(getContext(), activityObj.getString("id"));
        Assert.assertNull(manager.findByActivityId(getContext(), activityObj.getString("id")));
    }

}
