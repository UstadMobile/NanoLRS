package com.ustadmobile.nanolrs.test.core;

import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiActivityEndpointCore;
import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiAgentEndpointCore;
import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiStateEndpointCore;
import com.ustadmobile.nanolrs.test.core.model.TestSync;
import com.ustadmobile.nanolrs.test.core.model.TestSyncComponents;
import com.ustadmobile.nanolrs.test.core.model.TestXapiForwardingStatement;
import com.ustadmobile.nanolrs.test.core.model.TestXapiStatement;
import com.ustadmobile.nanolrs.test.core.model.TestUser;
import com.ustadmobile.nanolrs.test.core.util.TestEntitiesToJsonArray;
import com.ustadmobile.nanolrs.test.core.util.TestJsonUtil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by mike on 5/6/17.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({

        TestXapiActivityEndpointCore.class,
        TestXapiAgentEndpointCore.class,
        TestXapiStateEndpointCore.class,
        TestXapiForwardingStatement.class,
        TestXapiStatement.class,
        TestUser.class,
        TestJsonUtil.class,
        TestSyncComponents.class,
        TestEntitiesToJsonArray.class,
        TestSync.class
})

public abstract class NanoLrsTestSuite {
}
