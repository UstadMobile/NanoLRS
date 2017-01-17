package com.ustadmobile.nanolrs.android.endpoints;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.endpoints.XapiActivityEndpoint;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created by mike on 10/6/16.
 */
public class TestXapiActivityEndpoint {

    @Before
    public void setUp() throws Exception {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
    }

    @Test
    public void testActivityEndpoint() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        InputStream activityIn = context.getAssets().open("test-activity.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(activityIn, writer, "UTF-8");
        activityIn.close();

        String activityStr = writer.toString();
        JSONObject activityObj = new JSONObject(activityStr);
        XapiActivity activity = XapiActivityEndpoint.createOrUpdate(context, activityObj);
        Assert.assertNotNull(activity);

        //now we should be able to find it by ID
        XapiActivityManager manager =PersistenceManager.getInstance().getActivityManager();
        XapiActivity found = manager.findByActivityId(context,
                activityObj.getString("id"));
        Assert.assertNotNull(found);

        //now delete it
        manager.deleteByActivityId(context, activityObj.getString("id"));
        Assert.assertNull(manager.findByActivityId(context, activityObj.getString("id")));
    }




}
