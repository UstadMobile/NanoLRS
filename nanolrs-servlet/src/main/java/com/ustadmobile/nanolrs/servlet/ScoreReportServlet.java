package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.io.IOException;
import java.io.PrintWriter;

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

public class ScoreReportServlet extends HttpServlet {

    public ScoreReportServlet() {
        super();
        System.out.println("In ScoreReportServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In ScoreReportServlet.doGet()..");

        //basic output:
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String name = request.getParameter("name");
        out.println(
                "<html><body>" +
                        "<h1>" + "Hi, " + name + "</h1>" +
                        "</body></html>");
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
        System.out.println("In ScoreReportServlet.doPost()..");
        Object dbContext = getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);

        //Get headers if any
        /*
        String userUuid = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_UUID);
        String username = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_USERNAME);
        String password = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_PASSWORD);
        String isNewUser = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_USER_IS_NEW);
        String nodeUuid = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_UUID);
        String nodetHostName = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_HOST);
        String nodeHostUrl = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_URL);
        String nodeRole = ServletUtil.getHeaderVal(req, UMSyncEndpoint.HEADER_NODE_ROLE);
        */

        String jsonToReturn = "{\n" +
            "    \"data\": [\n" +
            "        {\"username\": \"user1\", \"name\": \"Chai Zakir\", \"university\" : 23, \"university_name\": \"Kabul University\", \"enrolled\": \"true\"},\n" +
            "        {\"username\": \"user2\", \"name\": \"Kafi Ismail\", \"university\" : 24, \"university_name\": \"AFG University\", \"enrolled\": \"false\"},\n" +
            "        {\"username\": \"user3\", \"name\": \"Saeb Salik\", \"university\" : 24, \"university_name\": \"AFG University\", \"enrolled\": \"true\"},\n" +
            "        {\"username\": \"user4\", \"name\": \"Aam Ali\", \"university\" : 23, \"university_name\": \"Kabul University\", \"enrolled\": \"false\"},\n" +
            "        {\"username\": \"user5\", \"name\": \"Kafi Ismail\", \"university\" : 24, \"university_name\": \"AFG University\", \"enrolled\": \"false\"}\n" +
            "        ]\n" +
            "    }";
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonToReturn);


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
