package com.ustadmobile.nanolrs.http;

import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by varuna on 7/20/2017.
 */

public class SyncUriResponder extends NanoLrsResponder{

    private String getFirstParamVal(NanoHTTPD.IHTTPSession session, String paramName) {
        if(session.getParameters().containsKey(paramName)) {
            return session.getParameters().get(paramName).get(0);
        }else {
            return null;
        }
    }

    private String getHeaderVal(NanoHTTPD.IHTTPSession session, String headerName){
        if(session.getHeaders().containsKey(headerName)) {
            return session.getHeaders().get(headerName);
        }
        return null;
    }

    public static String convertStreamToString2(InputStream is, String encoding) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, encoding);
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }
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


    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource,
                                   Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        Object dbContext = uriResource.initParameter(0, Object.class);
        String thisURL = "http://" + session.getRemoteHostName() + session.getUri();

        //TODO: send content from post request into handleIncomingSync, return reply
        byte[] postBodyReceived = NanoLrsHttpd.getRequestContent(session);
        //send the data received to the handleIncomingSync method

        String userUuid = getHeaderVal(session, "useruuid");
        String username = getHeaderVal(session, "username");
        String password = getHeaderVal(session, "password");
        String isNewUser = getHeaderVal(session, "isnewuser");
        String nodeUuid = getHeaderVal(session, "nodeuuid");
        String requestHostName = getHeaderVal(session, "hostname");
        String requestHostUrl = getHeaderVal(session, "hosturl");

        //could send name, etc
        PersistenceManager pm = PersistenceManager.getInstance();

        UserManager userManager = pm.getManager(UserManager.class);

        if (userManager.authenticate(dbContext, username, password) == false){
            if(userManager.findByUsername(dbContext, username) == null ||
                    userManager.findByUsername(dbContext, username).isEmpty()){
                if(isNewUser.equals("true")){
                    //Create the new user
                    try {
                        User newUser = (User)userManager.makeNew();
                        newUser.setUuid(userUuid);
                        newUser.setUsername(username);
                        newUser.setPassword(password);
                        userManager.persist(dbContext, newUser);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        NodeManager nodeManager = pm.getManager(NodeManager.class);
        Node node = null;

        try {
            node = (Node) nodeManager.findByPrimaryKey(dbContext, nodeUuid);

            if(node == null){
                //Create this new node
                node = (Node)nodeManager.makeNew();
                node.setUUID(nodeUuid);
                node.setUrl(thisURL);
                node.setName(requestHostName);
                node.setRole(requestHostUrl);
                nodeManager.persist(dbContext, node);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //postBodyReceived
        ByteArrayInputStream sessionStream = new ByteArrayInputStream(postBodyReceived);

        UMSyncResult result = null;
        try {
            result = UMSyncEndpoint.handleIncomingSync(
                    sessionStream,
                    node,
                    session.getHeaders(),
                    session.getParameters(),
                    dbContext);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        NanoHTTPD.Response.IStatus status = null;
        switch(result.getStatus()) {
            case 200:
                status = NanoHTTPD.Response.Status.OK;
                break;
            default:
                status = RouterNanoHTTPD.Response.Status.INTERNAL_ERROR;
        }
        return NanoHTTPD.newFixedLengthResponse(status, "application/json", result.getResponseData(),
                result.getResponseLength());
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }
}