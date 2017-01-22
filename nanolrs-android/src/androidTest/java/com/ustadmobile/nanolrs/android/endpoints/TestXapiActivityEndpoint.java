package com.ustadmobile.nanolrs.android.endpoints;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.endpoints.TestXapiActivityEndpointCore;

import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.junit.Before;

/**
 * Created by mike on 10/6/16.
 */
public class TestXapiActivityEndpoint extends TestXapiActivityEndpointCore {

    @Before
    public void setUp() throws Exception {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
    }

    @Override
    public Object getContext() {
        return InstrumentationRegistry.getContext();
    }


}
