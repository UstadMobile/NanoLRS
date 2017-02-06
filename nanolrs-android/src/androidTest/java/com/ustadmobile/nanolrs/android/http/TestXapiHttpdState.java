package com.ustadmobile.nanolrs.android.http;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.http.HttpLrs;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.nanolrs.httpd.TestXapiHttpdStateHttp;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by mike on 10/9/16.
 */

public class TestXapiHttpdState extends TestXapiHttpdStateHttp {

    @Override
    protected void setPersistenceManager() {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
    }

    @Override
    public Object getContext() {
        return InstrumentationRegistry.getContext();
    }

    @Test
    public void testSomething() {
        Assert.assertTrue(true);
    }

}
