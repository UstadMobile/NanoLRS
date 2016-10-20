package com.ustadmobile.nanolrs.http;

import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
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

public class StatementsUriResponder implements RouterNanoHTTPD.UriResponder {

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
            Map<String, String> map = new HashMap<>();
            session.parseBody(map);
            String tmpFile =  map.get("content");
            StringWriter strWriter = new StringWriter();
            fin = new FileInputStream(tmpFile);
            IOUtils.copy(fin, strWriter, "UTF-8");

            JSONObject stmtObj = new JSONObject(strWriter.toString());
            Object dbContext = uriResource.initParameter(0, Object.class);
            String storedId = XapiStatementsEndpoint.putStatement(stmtObj, dbContext);

            //TODO: check that storedID is the same as the statement's id
            r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NO_CONTENT, "text/plain",
                null);
        }catch(IOException|NanoHTTPD.ResponseException e) {
            r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain",
                    e.getMessage());
        }finally {
            IOUtils.closeQuietly(fin);
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
