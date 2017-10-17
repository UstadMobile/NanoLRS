package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.ustadmobile.nanolrs.util.ServletUtil.*;

/**
 * Created by varuna on 7/25/2017.
 */

public class UMSyncServlet extends HttpServlet {

    public UMSyncServlet() {
        super();
        System.out.println("In UMSyncServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In UMSyncServlet.doGet()..");
        response.sendRedirect("home/");

    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Object dbContext =
                getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        String userUuid = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_UUID);
        String username = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_USERNAME);
        String password = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_PASSWORD);
        String isNewUser = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_IS_NEW);

        String nodeUuid = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_UUID);
        String nodetHostName = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_HOST);
        String nodeHostUrl = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_URL);
        String nodeRole = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_ROLE);

        String basicAuth = getHeaderVal(req, UMSyncEndpoint.REQUEST_AUTHORIZATION);
        String syncStatus = getHeaderVal(req, UMSyncEndpoint.RESPONSE_SYNCED_STATUS);

        PersistenceManager pm = PersistenceManager.getInstance();

        UserManager userManager = pm.getManager(UserManager.class);

        /*** EXPLANATION OF HASHING PASSWORD LOGIC
         *
            //Note: In the case where when user is null (ie new user) but using old app:
            //the password wil be saved at endpoint as a hash (we hash it). but not as a
            //hash on device (its still plain text on device) until app on device is updated.
            //Which means:
            // i. We can either update the local seq here so the user details
            //      are an update to device.
            //      In that case the hash password is updated at device (hash).
            // But the problem is:
            // >>> Upon second sync, the hash will not be in basic auth. We don't
            //      want to double hash it.
            //SOLUTION:
            // iii. We DON'T UPDATE THE LOCAL SEQ TO MARK THE USER AS AN UPDATE AT ENDPOINT.
            // If user password is hashed on endpoint, there is no point updating it on device
            // with an old app
            //
            //ie: no point syncing it as the password on device will be a hash and login
            //system will fail on device. That means we hash password only when they update the app.
            //that's why we're not +1 ing the change seq such that the hashed password gets updated
            //to all devices upon next sync.
            // Basically the new version of the app is a critical update.
        */

        //Get username and password from basic authentication :
        String authUsername = getUsernameFromBasicAuth(req);
        String authPassword = getPasswordFromBasicAuth(req);

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

        //Always authenticate via hashed password because of Assumption 2.
        if (userManager.authenticate(dbContext, username, password, true) == false){
            User existingUser = userManager.findByUsername(dbContext, username);
            if(existingUser == null){
                if(isNewUser.equals("true")){
                    //Create the new user
                    try {
                        User newUser = (User)userManager.makeNew();
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
            }else{
                if (!node.getHost().equals(nodeHostUrl)){
                    node.setHost(nodeHostUrl);
                    nodeManager.persist(dbContext, node);
                }
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

        //todo test these?
        reqHeaders.put(UMSyncEndpoint.RESPONSE_SYNCED_STATUS, syncStatus);
        reqHeaders.put(UMSyncEndpoint.REQUEST_AUTHORIZATION, basicAuth);

        //Form parameters (if applicable)
        Map<String, String> reqParams = new HashMap<>();

        //Form InputStream from request
        InputStream reqInputStream = req.getInputStream();

        //Handle Incoming Sync:
        UMSyncResult result = null;
        try {
            result = UMSyncEndpoint.handleIncomingSync(
                    reqInputStream,
                    node,
                    reqHeaders,
                    reqParams,
                    dbContext);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String statusMessage="";
        switch(result.getStatus()) {
            case 200:
                //Update only if sync OK
                if(UMSyncEndpoint.getHeader(result.getHeaders(),
                        UMSyncEndpoint.RESPONSE_SYNCED_STATUS).equals(UMSyncEndpoint.RESPONSE_SYNC_OK)){
                    //Update sync status
                    System.out.println("UMSyncServlet: Incoming Sync OK. Updating SyncStatus");
                    resp.setHeader(UMSyncEndpoint.RESPONSE_SYNCED_STATUS,
                            UMSyncEndpoint.RESPONSE_SYNC_OK);
                    try {
                        UMSyncEndpoint.updateSyncStatus(result, node, dbContext);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                statusMessage.equals("OK");
                break;
            default:
                statusMessage.equals("INTERNAL ERROR");
        }

        //set resp data: result.getResponseData() and length result.getResponseLength()
        resp.setStatus(result.getStatus());
        resp.setContentType("application/json");
        resp.setHeader("key", "value");
        resp.setContentLength((int)result.getResponseLength());

        ServletOutputStream sos = resp.getOutputStream();

        String responseDataString = convertStreamToString(result.getResponseData(),
                UMSyncEndpoint.UTF_ENCODING);
        byte[] responseDataBytes = responseDataString.getBytes();
        sos.write(responseDataBytes);
        sos.close();

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doTrace(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {
        super.service(req, res);
    }
}
