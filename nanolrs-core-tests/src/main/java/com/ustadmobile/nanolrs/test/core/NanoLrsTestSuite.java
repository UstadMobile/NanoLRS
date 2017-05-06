package com.ustadmobile.nanolrs.test.core;

import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiActivityEndpointCore;
import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiAgentEndpointCore;
import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiStateEndpointCore;
import com.ustadmobile.nanolrs.test.core.model.TestXapiForwardingStatement;
import com.ustadmobile.nanolrs.test.core.model.TestXapiStatement;
import com.ustadmobile.nanolrs.test.core.model.TestXapiUser;

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
        TestXapiUser.class
})

public abstract class NanoLrsTestSuite {
}
