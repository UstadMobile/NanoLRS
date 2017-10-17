package com.ustadmobile.nanolrs.http;

import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.core.util.Base64CoderNanoLrs;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by varuna on 7/20/2017.
 * Were just using this for tests.. This is not complete code for sync handling !
 */

public class SyncUriResponder extends NanoLrsResponder{

    private String getFirstParamVal(NanoHTTPD.IHTTPSession session, String paramName) {
        if(session.getParameters().containsKey(paramName)) {
            return session.getParameters().get(paramName).get(0);
        }else {
            return null;
        }
    }

    /**
     * Get username from basic auth in request
     * @param basicAuth
     * @return
     */
    public static String getUsernameFromBasicAuth(String basicAuth){
        String[] credentials = getCredentialStringFromBasicAuth(basicAuth);
        if(credentials != null && credentials.length > 0) {
            return credentials[0];
        }
        return null;
    }

    /**
     * Get password from basic auth in request
     * @param basicAuth
     * @return
     */
    public static String getPasswordFromBasicAuth(String basicAuth){
        String[] credentials = getCredentialStringFromBasicAuth(basicAuth);
        if(credentials != null && credentials.length > 0) {
            return credentials[1];
        }
        return null;
    }

    /**
     * Get username and password string from request's basic auth
     * @param authorization
     * @return
     */
    public static String[] getCredentialStringFromBasicAuth(String authorization) {

        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = Base64CoderNanoLrs.decodeString(base64Credentials);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            return values;
        }
        return null;
    }
    public static String convertStreamToString2(InputStream is, String encoding)
            throws IOException {
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
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String,
            String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String,
            String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource,
                                   Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        Object dbContext = uriResource.initParameter(0, Object.class);
        String thisURL = "http://" + session.getRemoteHostName() + session.getUri();

        byte[] postBodyReceived = NanoLrsHttpd.getRequestContent(session);
        //send the data received to the handleIncomingSync method

        String userUuid = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_USER_UUID);
        String username = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_USER_USERNAME);
        String password = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_USER_PASSWORD);
        String isNewUser = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_USER_IS_NEW);
        String nodeUuid = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_NODE_UUID);
        String nodetHostName = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_NODE_HOST);
        String nodeHostUrl = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_NODE_URL);
        String nodeRole = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.HEADER_NODE_ROLE);
        String basicAuth = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.REQUEST_AUTHORIZATION);
        String syncStatus = UMSyncEndpoint.getHeader(session.getHeaders(),
                UMSyncEndpoint.RESPONSE_SYNCED_STATUS);

        //could send name, etc
        PersistenceManager pm = PersistenceManager.getInstance();

        UserManager userManager = pm.getManager(UserManager.class);

        //Get username and password from basic authentication :
        String authUsername = getUsernameFromBasicAuth(basicAuth);
        String authPassword = getPasswordFromBasicAuth(basicAuth);

        //With the new app, only real cred (text password) is in basic auth.
        //In the old app, real cred (text password) is in header.
        // Note: Laid out below for explanation.
        if(authUsername != null && authPassword != null){ //if basic auth exists
            username = authUsername;
            password = authPassword; //the plain text password from basic auth.

            //Assumption 1: Since we got this cred from BASIC Auth,
            // its safe to assume that the password on device is hashed as well.
            //Assumption 2: Endpoint Database passwords are all hashed (and they should be).

        }
        //Assumption 2: Endpoint DB passwords are all hashed (and they should be).

        //Assumption 3: If no Basic Auth present in request (old app),
        // password gotten from header which is also plain text. Password on
        // device is not hashed.


        //We hash it cause all passwords stored in endpoint db are hashed.

        User newUser = null;
        //Always authenticate via hashed password because of Assumption 2.
        if (userManager.authenticate(dbContext, username, password, true) == false){
            User existingUser = userManager.findByUsername(dbContext, username);
            if(existingUser == null){
                if(isNewUser.equals("true")){
                    //Create the new user
                    try {
                        newUser = (User)userManager.makeNew();
                        newUser.setUuid(userUuid);
                        newUser.setUsername(username);

                        //Always hash passwords at endpoint db. (Assumption 2)
                        if(password != null && !password.isEmpty()){
                            try {
                                //hash it.
                                password = userManager.hashPassword(password);
                            } catch (NoSuchAlgorithmException e) {
                                System.out.println("Cannot hash password.: " + e);
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

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
                node.setUrl(nodeHostUrl);
                node.setName(nodetHostName);
                node.setHost(nodetHostName);
                /* Role is always local */
                node.setRole(nodeRole); //it will mostly be "client"
                if(nodeRole.equals("proxy")){
                    node.setProxy(true);
                }
                if(nodeRole.equals("main")){
                    node.setMaster(true);
                }
                nodeManager.persist(dbContext, node);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Form headers from request
        Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_UUID, userUuid);
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_USERNAME, username);
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_PASSWORD, password);
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_IS_NEW, isNewUser);

        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_UUID, nodeUuid);
        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_HOST, nodetHostName);
        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_URL, nodeHostUrl);
        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_ROLE, nodeRole);

        reqHeaders.put(UMSyncEndpoint.RESPONSE_SYNCED_STATUS, syncStatus);
        reqHeaders.put(UMSyncEndpoint.REQUEST_AUTHORIZATION, basicAuth);

        //Form parameters (if applicable)
        Map<String, String> reqParams = new HashMap<>();

        //postBodyReceived
        ByteArrayInputStream sessionStream = new ByteArrayInputStream(postBodyReceived);

        UMSyncResult result = null;
        try {
            result = UMSyncEndpoint.handleIncomingSync(
                    sessionStream,
                    node,
                    //session.getHeaders(),
                    reqHeaders,
                    session.getParameters(),
                    dbContext);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NanoHTTPD.Response.IStatus status = null;
        syncStatus = UMSyncEndpoint.RESPONSE_SYNC_FAIL;
        switch(result.getStatus()) {
            case 200:
                status = NanoHTTPD.Response.Status.OK;
                syncStatus = UMSyncEndpoint.RESPONSE_SYNC_OK;
                break;
            default:
                status = RouterNanoHTTPD.Response.Status.INTERNAL_ERROR;
                if(newUser != null){
                    //Delete user created.
                    userManager.delete(dbContext, newUser);
                }
        }
        status = NanoHTTPD.Response.Status.lookup(result.getStatus());

        NanoHTTPD.Response added = NanoHTTPD.newFixedLengthResponse(status, "application/json",
                result.getResponseData(),
                result.getResponseLength());


        Map<String, String> sessionHeaders = session.getHeaders();
        Iterator<Map.Entry<String, String>> iterator = sessionHeaders.entrySet().iterator();
        long headerSize = 0;
        while(iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            headerSize = headerSize + entry.getKey().length() + entry.getValue().length();
        }

        added.addHeader(UMSyncEndpoint.RESPONSE_SYNCED_STATUS, syncStatus);
        return added;
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String,
            String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }
}
