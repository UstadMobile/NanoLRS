package com.ustadmobile.nanolrs.test.core;


import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiActivityEndpointCore;
import com.ustadmobile.nanolrs.test.core.endpoints.TestXapiAgentEndpointCore;

/**
 * Base class for tests that allows for a core test to access implementation dependent context. Our
 * logic uses a system dependent context object : e.g. Context on Android, at least sometimes
 * AppDelegate on iOS. A test also tests the core functionality that is implementation independent.
 * Java does not support multiple inheritence.
 *
 *
 * Created by mike on 1/21/17.
 */

public abstract class NanoLrsCoreTest {

    public static final Class[] CORE_TESTS = new Class[]{
        TestXapiActivityEndpointCore.class,
        TestXapiAgentEndpointCore.class
    };

}
