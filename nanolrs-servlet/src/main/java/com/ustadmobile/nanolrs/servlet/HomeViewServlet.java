package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.util.ServletUtil;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by varuna on 7/25/2017.
 */

public class HomeViewServlet extends HttpServlet {

    public HomeViewServlet() {
        super();
        System.out.println("In HomeViewServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In HomeViewServlet.doGet()..");

        response.sendRedirect("../Home.jsp");
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
        System.out.println("In HomeViewServlet.doPOST()..");
        Object dbContext = getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);

        //Get headers if any
        String userUuid = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_UUID);
        String username = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_USERNAME);
        String password = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_PASSWORD);
        String isNewUser = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_IS_NEW);
        String nodeUuid = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_UUID);
        String nodetHostName = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_HOST);
        String nodeHostUrl = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_URL);
        String nodeRole = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_ROLE);


        //Disable it like so:
        super.doPost(req, resp);

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
