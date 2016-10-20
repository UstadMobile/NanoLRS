package com.ustadmobile.nanolrs.http;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
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
        mountXapiEndpointsOnServer(this, dbContext, basePath);
    }

    /**
     * Mount the xAPI server endpoint responders to the given RouterNanoHTTPD server
     *
     * @param server Server to mount to
     * @param dbContext Database context to use for Orm purposes
     * @param basePath prefix for xAPI endpoints (e.g. /xapi)
     */
    public static void mountXapiEndpointsOnServer(RouterNanoHTTPD server, Object dbContext, String basePath) {
        if(!basePath.endsWith("/")) {
            basePath += "/";
        }

        server.addRoute(basePath + "statements", StatementsUriResponder.class, dbContext);
        server.addRoute(basePath + "activities/state", StateUriResponder.class, dbContext);
    }

    public static byte[] getRequestContent(NanoHTTPD.IHTTPSession session) {
        byte[] content = null;
        FileInputStream fin = null;
        String tmpFileName;
        try {
            Map<String, String> map = new HashMap<>();
            session.parseBody(map);
            tmpFileName =  map.get("content");
            fin = new FileInputStream(tmpFileName);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(fin, bout);
            bout.flush();
            content = bout.toByteArray();
        }catch(IOException | NanoHTTPD.ResponseException e) {
            System.err.println("Exception getRequestContent");
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(fin);
        }

        return content;
    }


}
