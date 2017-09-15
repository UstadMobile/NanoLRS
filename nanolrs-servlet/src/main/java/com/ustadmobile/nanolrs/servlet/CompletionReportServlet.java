package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.util.MappingValues;
import com.ustadmobile.nanolrs.util.ServletUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by varuna on 7/25/2017.
 */

public class CompletionReportServlet extends HttpServlet {


    public static final String UNIVERSITY_FILTER_NAME = "universities_filter_names[]";

    public CompletionReportServlet() {
        super();
        System.out.println("In CompletionReportServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In CompletionReportServlet.doGet()..");

        //TODO: automate this.
        Map<String, String> table_headers_html = new LinkedHashMap<>();
        table_headers_html.put(MappingValues.USER_COLUMN_FULLNAME, "Name");
        table_headers_html.put(MappingValues.USER_COLUMN_USERNAME, "Username");
        table_headers_html.put(MappingValues.USER_COLUMN_UNIVERSITY, "University");
        table_headers_html.put(MappingValues.USER_COLUMN_TAZKIRA_ID, "Tazkira ID");
        table_headers_html.put(MappingValues.USER_COLUMN_GENDER, "Gender");
        table_headers_html.put(MappingValues.MODULE_1_SHORTID, MappingValues.MODULE_1_NAME);
        table_headers_html.put(MappingValues.MODULE_2_SHORTID, MappingValues.MODULE_2_NAME);
        table_headers_html.put(MappingValues.MODULE_3_SHORTID, MappingValues.MODULE_3_NAME);
        table_headers_html.put(MappingValues.MODULE_4_SHORTID, MappingValues.MODULE_4_NAME);

        HttpSession session=request.getSession();
        String sessionAdmin = (String)session.getAttribute(MappingValues.SUPER_ADMIN_USERNAME);
        if(sessionAdmin != null){
            if(sessionAdmin.equals(MappingValues.SUPER_ADMIN_USERNAME)){
                request.setAttribute("table_headers_html",table_headers_html);
                request.setAttribute("static","/syncendpoint/");
                request.setAttribute("universities", MappingValues.universities);
                request.getSession().setAttribute("table_headers_html",table_headers_html);
                request.getSession().setAttribute("static","/syncendpoint/");
                request.getSession().setAttribute("universities", MappingValues.universities);

                //response.sendRedirect("../CompletionReport.jsp");
                System.out.println("Forwarding..");

                request.getRequestDispatcher("../CompletionReport.jsp").forward(request, response);
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
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doHead(req, resp);
    }

    public boolean agentModulePresentInStatement(XapiAgent agent, String verb, List<String> activities, Object dbContext){
        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);
        XapiStatementManager statementManager = pm.getManager(XapiStatementManager.class);
        XapiAgentManager agentManager = pm.getManager(XapiAgentManager.class);
        UserCustomFieldsManager ucfManager = pm.getManager(UserCustomFieldsManager.class);

        if(agent == null){
            return false;
        }

        /*
        Object dbContext, String statementid, String voidedStatemendid,
        XapiAgent agent, String verb, String activity, String registration,
        boolean relatedActivities, boolean relatedAgents, long since,
        long until, int limit);
         */
        Iterator<String> activitiesIterator = activities.iterator();
        //List allStatements = new ArrayList();
        boolean result = false;
        while(activitiesIterator.hasNext()){
            String activity = activitiesIterator.next();
            List statements = statementManager.findByParams(dbContext, null, null, agent, verb,
                    activity, null, false, false, -1, -1, -1);
            if(!statements.isEmpty()){
                result = true;
            }
        }

        return result;

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("In CompletionReportServlet.doPost()..");
        Object dbContext = getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);
        XapiAgentManager agentManager = pm.getManager(XapiAgentManager.class);
        UserCustomFieldsManager ucfManager = pm.getManager(UserCustomFieldsManager.class);

        List<User> allUsers = null;
        String jsonToReturn = "";
        XapiAgent agent = null;
        JSONArray userEnrollmentJSONArray = new JSONArray();

        String[] uni_names = req.getParameterValues(UNIVERSITY_FILTER_NAME);

        try {
            allUsers = userManager.getAllEntities(dbContext);

            //Get choosen uni filter
            ArrayList allChoosenUniNames = new ArrayList();
            if(uni_names != null) {
                if(uni_names.length > 0){
                    for (int k = 0; k < uni_names.length; k++) {
                        String choosenUniName = uni_names[k];
                        allChoosenUniNames.add(choosenUniName);
                    }
                }
            }

            //Loop over every User:
            for(User user:allUsers) {
                String username = user.getUsername();
                if(username.equals("admin")){
                    //Don't show admins stuff
                    continue;
                }

                //Don't show testing users
                if(user.getNotes() != null){
                    if(user.getNotes().equals("testing")) {
                        //Don't show testing users
                        continue;
                    }
                }

                //Get agent:
                List<XapiAgent> agents = agentManager.findByUser(dbContext, user);
                if(agents != null && !agents.isEmpty()){
                    agent = agents.get(0);
                }else{
                    agent = null;
                }

                JSONObject userInfoJSON = ServletUtil.getUserInfo(user, dbContext);

                /* University Filter */
                String user_university = ucfManager.getUserField(
                        user, MappingValues.custom_fields_map.get("university"), dbContext);

                boolean iWantToBreakFree =
                        ServletUtil.shouldIShowThisUserWithFilter(user_university, allChoosenUniNames);
                if(iWantToBreakFree){
                    continue;
                }

                String m1result= "";
                String m2result = "";
                String m3result = "";
                String m4result = "";

                boolean m1resultpass = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_1_IDS, dbContext);
                boolean m1resultfail = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_1_IDS, dbContext);
                if(m1resultpass){
                    m1result="true";
                }else if(m1resultfail){
                    m1result = "false";
                }

                boolean m2resultpass = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_2_IDS, dbContext);
                boolean m2resultfail = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_2_IDS, dbContext);
                if(m2resultpass){
                    m2result="true";
                }else if(m2resultfail){
                    m2result = "false";
                }

                boolean m3resultpass = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_3_IDS, dbContext);
                boolean m3resultfail = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_3_IDS, dbContext);
                if(m3resultpass){
                    m3result="true";
                }else if(m3resultfail){
                    m3result = "false";
                }

                boolean m4resultpass = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_4_IDS, dbContext);
                boolean m4resultfail = agentModulePresentInStatement(agent,
                        MappingValues.XAPI_PASSED_VERB, MappingValues.MODULE_4_IDS, dbContext);
                if(m4resultpass){
                    m4result="true";
                }else if(m4resultfail){
                    m4result = "false";
                }

                userInfoJSON.put("m1",m1result);
                userInfoJSON.put("m2",m2result);
                userInfoJSON.put("m3",m3result);
                userInfoJSON.put("m4",m4result);

                userEnrollmentJSONArray.put(userInfoJSON);
            }

            jsonToReturn = userEnrollmentJSONArray.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION!");
        }

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
