package com.ustadmobile.nanolrs.test.core.model;

import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;
import org.junit.Assert;

import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 2/7/17.
 */

public class TestUser {
    @Test
    public void testLifecycle() throws Exception {
        Object context = NanoLrsPlatformTestUtil.getContext();
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        User newUser = userManager.createSync(context, UUID.randomUUID().toString());
        newUser.setUsername("testuser");
        userManager.persist(context, newUser);
        List<User> usernameList = userManager.findByUsername(context, "testuser");
        Assert.assertEquals(usernameList.size(), 1);
        Assert.assertEquals(usernameList.get(0).getUsername(), "testuser");
        userManager.delete(context, newUser);
        usernameList = userManager.findByUsername(context, "testuser");
        Assert.assertEquals(usernameList.size(), 0);
    }
}
