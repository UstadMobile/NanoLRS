package com.ustadmobile.nanolrs.android.endpoints;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.endpoints.XapiAgentEndpoint;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by mike on 10/6/16.
 */

public class TestXapiAgentEndpoint {

    @Before
    public void setUp() throws Exception {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
    }


    @Test
    public void testXapiAgentEndpoint() throws Exception {
        Context context = InstrumentationRegistry.getContext();

        JSONObject mboxAgent = new JSONObject();
        mboxAgent.put("mbox", "mailto:mike@ustadmobile.com");
        XapiAgentManager manager = PersistenceManager.getInstance().getAgentManager();
        XapiAgent mboxAgentProxy= XapiAgentEndpoint.createOrUpdate(context, mboxAgent);
        Assert.assertNotNull(mboxAgentProxy);

        List<XapiAgent> agents = manager.findAgentByParams(context, "mailto:mike@ustadmobile.com", null, null);
        Assert.assertTrue(agents.size() > 0);

        JSONObject accountAgent = new JSONObject();
        JSONObject accountObj = new JSONObject();
        accountObj.put("homePage", "http://ustadmobile.com");
        accountObj.put("name", "bob");
        accountAgent.put("account", accountObj);
        XapiAgent accountAgentProxy = XapiAgentEndpoint.createOrUpdate(context, accountAgent);
        agents = manager.findAgentByParams(context, null, "bob", "http://ustadmobile.com");
        Assert.assertTrue(agents.size() > 0);



    }

}
