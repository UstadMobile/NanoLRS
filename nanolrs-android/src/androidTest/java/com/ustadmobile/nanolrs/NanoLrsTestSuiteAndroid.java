package com.ustadmobile.nanolrs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiActivityEndpointCore;
import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiAgentEndpointCore;


/**
 * Created by mike on 5/6/17.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestXapiActivityEndpointCore.class,
        TestXapiAgentEndpointCore.class
})
public class NanoLrsTestSuiteAndroid {
}
