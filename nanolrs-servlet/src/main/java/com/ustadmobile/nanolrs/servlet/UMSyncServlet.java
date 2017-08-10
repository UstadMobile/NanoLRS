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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In UMSyncServlet.doGet()..");

        response.sendRedirect("home/");

        /*
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String name = request.getParameter("name");
        out.println(
                "<html><body>" +
                        "<h1>" + "Hi, " + name + "</h1>" +
                        "</body></html>");
        */
        //super.doGet(request, response);

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object dbContext = getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        String userUuid = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_UUID);
        String username = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_USERNAME);
        String password = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_PASSWORD);
        String isNewUser = getHeaderVal(req, UMSyncEndpoint.HEADER_USER_IS_NEW);

        String nodeUuid = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_UUID);
        String nodetHostName = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_HOST);
        String nodeHostUrl = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_URL);
        String nodeRole = getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_ROLE);

        PersistenceManager pm = PersistenceManager.getInstance();

        UserManager userManager = pm.getManager(UserManager.class);

        if (userManager.authenticate(dbContext, username, password) == false){
            User existingUser = userManager.findByUsername(dbContext, username);
            if(existingUser == null){
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

        Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_UUID, userUuid);
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_USERNAME, username);
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_PASSWORD, password);
        reqHeaders.put(UMSyncEndpoint.HEADER_USER_IS_NEW, isNewUser);

        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_UUID, nodeUuid);
        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_HOST, nodetHostName);
        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_URL, nodeHostUrl);
        reqHeaders.put(UMSyncEndpoint.HEADER_NODE_ROLE, nodeRole);

        Map<String, String> reqParams = new HashMap<>();

        //reqHeaders = getHeadersFromRequest(req);
        //reqParams = getParamsFromRequest(req);

        InputStream reqInputStream = req.getInputStream();

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
        //TODO: this Check this
        String responseDataString = convertStreamToString(result.getResponseData(), UMSyncEndpoint.UTF_ENCODING);
        byte[] responseDataBytes = responseDataString.getBytes();
        sos.write(responseDataBytes);
        sos.close();


    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doTrace(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        super.service(req, res);
    }
}
