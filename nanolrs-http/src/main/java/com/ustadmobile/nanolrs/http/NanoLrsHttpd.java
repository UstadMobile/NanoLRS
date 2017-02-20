package com.ustadmobile.nanolrs.http;

import com.ustadmobile.nanolrs.core.util.LrsIoUtils;

import java.io.ByteArrayOutputStream;

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

    /**
     * Gets the payload of a request body.  When NanoHTTPD is sent a PUT request this seems to
     * wind up providing a temporary file pointed to by "content".  When receiving POST postData
     * seems to contain the content.
     *
     * @param session
     * @return
     */
    public static byte[] getRequestContent(NanoHTTPD.IHTTPSession session) {
        byte[] content = null;
        FileInputStream fin = null;
        String tmpFileName;
        try {
            Map<String, String> map = new HashMap<>();
            session.parseBody(map);
            if(map.containsKey("content")) {
                tmpFileName =  map.get("content");
                fin = new FileInputStream(tmpFileName);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();

                LrsIoUtils.copy(fin, bout);
                bout.flush();
                content = bout.toByteArray();
            }else if(map.containsKey("postData")) {
                content = map.get("postData").getBytes("UTF-8");
            }
        }catch(IOException | NanoHTTPD.ResponseException e) {
            System.err.println("Exception getRequestContent");
            e.printStackTrace();
        }finally {
            LrsIoUtils.closeQuietly(fin);
        }

        return content;
    }


}
