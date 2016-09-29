package com.ustadmobile.nanolrs.http;

import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 9/27/16.
 */

public class NanoLrsHttpd extends RouterNanoHTTPD {

    private Object dbContext;

    public NanoLrsHttpd(int portNum, Object dbContext) {
        super(portNum);
        this.dbContext = dbContext;
    }

    public void mapXapiEndpoints(String basePath) {
        if(!basePath.endsWith("/")) {
            basePath += "/";
        }

        addRoute(basePath + "statements", StatementsUriResponder.class, dbContext);
    }

}
