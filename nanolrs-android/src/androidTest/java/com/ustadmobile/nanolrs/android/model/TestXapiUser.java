package com.ustadmobile.nanolrs.android.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 9/27/16.
 */
@RunWith(AndroidJUnit4.class)
public class TestXapiUser {


    @Test
    public void testLifecycle() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        XapiUserManager userManager = PersistenceManager.getInstance().getUserManager();
        XapiUser newUser = userManager.createSync(context, UUID.randomUUID().toString());
        newUser.setUsername("testuser");
        userManager.persist(context, newUser);
        List<XapiUser> usernameList = userManager.findByUsername(context, "testuser");
        Assert.assertEquals(usernameList.size(), 1);
        Assert.assertEquals(usernameList.get(0).getUsername(), "testuser");
        userManager.delete(context, newUser);
        usernameList = userManager.findByUsername(context, "testuser");
        Assert.assertEquals(usernameList.size(), 0);
    }


}
