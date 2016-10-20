package com.ustadmobile.nanolrs.http;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 10/20/16.
 */

public abstract class NanoLrsResponder implements RouterNanoHTTPD.UriResponder {

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        if(method.equalsIgnoreCase("options")) {
            NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", null);
            response.addHeader("Allow", "GET,PUT,POST,DELETE");
            return response;
        }


        return null;
    }

}
