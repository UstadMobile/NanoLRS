package com.ustadmobile.nanolrs.http;

import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Init Parameter 0 = Database Context object
 *
 * Created by mike on 9/27/16.
 */

public class StatementsUriResponder extends NanoLrsResponder implements RouterNanoHTTPD.UriResponder {

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", "OK");
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String stmtId = urlParams.get("id");
        NanoHTTPD.Response r = null;
        FileInputStream fin = null;
        try {
            byte[] requestContent = NanoLrsHttpd.getRequestContent(session);
            JSONObject stmtObj = new JSONObject(new String(requestContent, "UTF-8"));
            Object dbContext = uriResource.initParameter(0, Object.class);
            String storedId = XapiStatementsEndpoint.putStatement(stmtObj, dbContext);

            //TODO: check that storedID is the same as the statement's id
            r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT, "text/plain",
                null);
        }catch(IOException e) {
            r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain",
                    e.getMessage());
        }catch(JSONException j) {
            r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain",
                    "JSON Parse exception: " + j.getMessage());
        }finally{
            LrsIoUtils.closeQuietly(fin);
        }

        return r;
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }




}
