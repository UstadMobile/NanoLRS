package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.NanoLrsPlatformTestUtil;

import junit.framework.Assert;

import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 2/7/17.
 */

public abstract class TestXapiUser {
    @Test
    public void testLifecycle() throws Exception {
        Object context = NanoLrsPlatformTestUtil.getContext();
        XapiUserManager userManager = PersistenceManager.getInstance().getManager(XapiUserManager.class);
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
