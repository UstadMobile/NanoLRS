package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.util.MappingValues;
import com.ustadmobile.nanolrs.util.Module;
import com.ustadmobile.nanolrs.util.ServletUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by varuna on 9/10/2017.
 */

public class UserInfoReportServlet extends HttpServlet {


    public static final String UNIVERSITY_FILTER_NAME = "universities_filter_names[]";
    public static final String LEGACY_MODE_FILTER_NAME = "legacy_mode";
    public static final String PASSED_STRING = "PASSED";
    public static final String FAILED_STRING = "FAILED";
    public static final String NA_STRING = "N/A";

    public UserInfoReportServlet() {
        super();
        System.out.println("In UserInfoReportServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In UserInfoReportServlet.doGet()..");

        //TODO: automate this.
        Map<String, String> table_headers_html = new LinkedHashMap<>();
        Map<String, String> table_headers_class = new LinkedHashMap<>();
        table_headers_html.put(MappingValues.USER_COLUMN_FULLNAME, "Name");
        table_headers_class.put(MappingValues.USER_COLUMN_FULLNAME, MappingValues.STICKY_CLASS_HTML);
        table_headers_html.put(MappingValues.USER_COLUMN_USERNAME, "Username");
        table_headers_class.put(MappingValues.USER_COLUMN_USERNAME, MappingValues.STICKY_CLASS_HTML);
        table_headers_html.put(MappingValues.USER_COLUMN_UNIVERSITY, "University");
        table_headers_class.put(MappingValues.USER_COLUMN_UNIVERSITY, MappingValues.STICKY_CLASS_HTML);
        table_headers_html.put(MappingValues.USER_COLUMN_TAZKIRA_ID, "Tazkira ID");
        table_headers_html.put(MappingValues.USER_COLUMN_GENDER, "Gender");



        HttpSession session=request.getSession();
        String sessionAdmin = (String)session.getAttribute(MappingValues.SUPER_ADMIN_USERNAME);
        if(sessionAdmin != null){
            if(sessionAdmin.equals(MappingValues.SUPER_ADMIN_USERNAME)){
                request.setAttribute("table_headers_html",table_headers_html);
                request.setAttribute("static","/syncendpoint/");
                request.setAttribute("universities", MappingValues.universities);

                request.getSession().setAttribute("table_headers_html",table_headers_html);
                request.getSession().setAttribute("table_headers_class",table_headers_class);
                request.getSession().setAttribute("static","/syncendpoint/");
                request.getSession().setAttribute("universities", MappingValues.universities);

                request.getRequestDispatcher("../UserInfoReport.jsp").forward(request, response);
            }else{
                response.sendRedirect("../../Login.jsp");
            }
        }else {
            response.sendRedirect("../../Login.jsp");
        }
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return super.getLastModified(req);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In UserInfoReportServlet.doPost()..");
        Object dbContext =
                getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);

        String jsonToReturn = "";
        JSONArray userEnrollmentJSONArray = new JSONArray();

        try {

            User user;
            String username = req.getParameter("username");
            System.out.println("Username is: " + username);

            user = userManager.findByUsername(dbContext, username);
            if(user != null) {
                JSONObject userInfoJSON = ServletUtil.getUserInfo(user, dbContext);
                userInfoJSON.put("blankspace", "");

                userEnrollmentJSONArray.put(userInfoJSON);

                jsonToReturn = userEnrollmentJSONArray.toString();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION!");
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonToReturn);

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
