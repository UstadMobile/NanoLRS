package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.NanoLRSCoreTest;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by mike on 2/6/17.
 */

public abstract class TestXapiAgentEndpointCore extends NanoLRSCoreTest {

    @Test
    public void testXapiAgentEndpoint() throws Exception {
        JSONObject mboxAgent = new JSONObject();
        mboxAgent.put("mbox", "mailto:mike@ustadmobile.com");
        XapiAgentManager manager = PersistenceManager.getInstance().getAgentManager();
        XapiAgent mboxAgentProxy= XapiAgentEndpoint.createOrUpdate(getContext(), mboxAgent);
        Assert.assertNotNull(mboxAgentProxy);

        List<XapiAgent> agents = manager.findAgentByParams(getContext(), "mailto:mike@ustadmobile.com", null, null);
        Assert.assertTrue(agents.size() > 0);

        JSONObject accountAgent = new JSONObject();
        JSONObject accountObj = new JSONObject();
        accountObj.put("homePage", "http://ustadmobile.com");
        accountObj.put("name", "bob");
        accountAgent.put("account", accountObj);
        XapiAgent accountAgentProxy = XapiAgentEndpoint.createOrUpdate(getContext(), accountAgent);
        agents = manager.findAgentByParams(getContext(), null, "bob", "http://ustadmobile.com");
        Assert.assertTrue(agents.size() > 0);



    }
}
