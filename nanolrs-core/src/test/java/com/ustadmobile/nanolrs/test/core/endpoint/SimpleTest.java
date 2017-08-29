package com.ustadmobile.nanolrs.test.core.endpoint;
/**
 * Created by varuna on 7/24/2017.
 */

import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

public class SimpleTest {
    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();

        //Write tests here..
        Assert.assertEquals(1,1);
    }
}
