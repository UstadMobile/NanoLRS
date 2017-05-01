package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.NanoLRSCoreTest;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;
import com.ustadmobile.nanolrs.core.util.NanoLrsPlatformTestUtil;

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
        Object context = NanoLrsPlatformTestUtil.getContext();
        InputStream activityIn = getClass().getResourceAsStream("/com/ustadmobile/nanolrs/core/test-activity.json");
        String activityStr = LrsIoUtils.inputStreamToString(activityIn);
        activityIn.close();

        JSONObject activityObj = new JSONObject(activityStr);
        XapiActivity activity = XapiActivityEndpoint.createOrUpdate(context, activityObj);
        Assert.assertNotNull("Can create a new activity", activity);

        //now we should be able to find it by ID
        XapiActivityManager manager = PersistenceManager.getInstance().getManager(XapiActivityManager.class);
        XapiActivity found = manager.findByActivityId(context,
                activityObj.getString("id"));
        Assert.assertNotNull("Can find new activity just created", found);

        //now delete it
        manager.deleteByActivityId(context, activityObj.getString("id"));
        Assert.assertNull("Activity not found after being deleted", manager.findByActivityId(context, activityObj.getString("id")));
    }

}
