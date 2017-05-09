package com.ustadmobile.nanolrs.test.http;


import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 5/9/17.
 */

public class TestStatementHttpd {

    private RouterNanoHTTPD server;


    @Before
    public void startServer() {
        server = new RouterNanoHTTPD(0);
        NanoLrsHttpd.mountXapiEndpointsOnServer(server, NanoLrsPlatformTestUtil.getContext(), "/xAPI");
    }

    @Test
    public void testServerRunning() {
        Assert.assertNotNull("Server not null", server);
    }

    @After
    public void stopServer() {
        server.stop();
        server = null;
    }


}
