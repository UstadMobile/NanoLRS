package com.ustadmobile.nanolrs.test.core.util;
/**
 * Created by varuna on 6/22/2017.
 */

import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class TestEntitiesToJsonArray {
    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();

        //Create some users
        XapiUserManager userManager =
                PersistenceManager.getInstance().getManager(XapiUserManager.class);

        //Get all entities since sequence number 0
        long sequenceNumber = 0;
        XapiUser currentUser = null;
        String host = "testing_host";

        /* Test that our list is not null and includes every entity */
        List<NanoLrsModel> allUsersSince = userManager.getAllSinceSequenceNumber(
                currentUser, context, host, sequenceNumber);
        Assert.assertNotNull(allUsersSince);

        XapiUser testThisUser = (XapiUser) allUsersSince.get(0);

        JSONObject thisEntityJson = ProxyJsonSerializer.toJson(
                testThisUser, XapiUser.class);
        Assert.assertNotNull(thisEntityJson);
    }
}
