package com.ustadmobile.nanolrs.test.core.util;
/**
 * Created by varuna on 6/22/2017.
 */

import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestEntitiesToJsonArray {
    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();

        //Create some users
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);

        //Get all entities since sequence number 0
        long sequenceNumber = 0;
        List<User> testingUsers = userManager.findByUsername(context, "testinguser");
        User testingUser = testingUsers.get(0);
        String host = "testing_host";

        /* Test that our list is not null and includes every entity */
        List<NanoLrsModel> allUsersSince = userManager.getAllSinceSequenceNumber(
                testingUser, context, host, sequenceNumber);
        Assert.assertNotNull(allUsersSince);

        User testThisUser = (User) allUsersSince.get(0);

        JSONObject thisEntityJson = ProxyJsonSerializer.toJson(
                testThisUser, User.class);
        Assert.assertNotNull(thisEntityJson);
    }
}
