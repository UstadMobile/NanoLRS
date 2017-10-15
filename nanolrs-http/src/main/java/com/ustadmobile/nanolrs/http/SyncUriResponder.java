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

    /*
    private String getHeaderVal(NanoHTTPD.IHTTPSession session, String headerName){

        //Enabling support for old header names.
        String oldHeaderName = null;
        if(headerName.startsWith("X-UM-")){
            oldHeaderName = headerName.substring("X-UM-".length(), headerName.length());
        }
        if(session.getHeaders().get(headerName) == null &&
                session.getHeaders().get(headerName.toLowerCase()) == null){
            String value = session.getHeaders().get(oldHeaderName);
            if(value!= null){
                System.out.println("OLD HEADER VALUE");
            }
            return value;
        }
        String val;
        val = session.getHeaders().get(headerName);
        if(val==null){
            val = session.getHeaders().get(headerName.toLowerCase());
        }

        return val;
    }
    */

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

        byte[] postBodyReceived = NanoLrsHttpd.getRequestContent(session);
        //send the data received to the handleIncomingSync method

        String userUuid = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_USER_UUID);
        String username = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_USER_USERNAME);
        String password = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_USER_PASSWORD);
        String isNewUser = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_USER_IS_NEW);
        String nodeUuid = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_NODE_UUID);
        String nodetHostName = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_NODE_HOST);
        String nodeHostUrl = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_NODE_URL);
        String nodeRole = UMSyncEndpoint.getHeader(session.getHeaders(), UMSyncEndpoint.HEADER_NODE_ROLE);

        //could send name, etc
        PersistenceManager pm = PersistenceManager.getInstance();

        UserManager userManager = pm.getManager(UserManager.class);


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
        } catch (IOException e) {
            e.printStackTrace();
        }

        NanoHTTPD.Response.IStatus status = null;
        String syncStatus = UMSyncEndpoint.RESPONSE_SYNC_FAIL;
        switch(result.getStatus()) {
            case 200:
                status = NanoHTTPD.Response.Status.OK;
                syncStatus = UMSyncEndpoint.RESPONSE_SYNC_OK;
                break;
            default:
                status = RouterNanoHTTPD.Response.Status.INTERNAL_ERROR;
        }
        status = NanoHTTPD.Response.Status.lookup(result.getStatus());

        NanoHTTPD.Response added = NanoHTTPD.newFixedLengthResponse(status, "application/json", result.getResponseData(),
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
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }
}
