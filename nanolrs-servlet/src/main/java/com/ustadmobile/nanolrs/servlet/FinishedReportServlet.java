package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.util.MappingValues;
import com.ustadmobile.nanolrs.util.Module;
import com.ustadmobile.nanolrs.util.ServletUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class FinishedReportServlet extends HttpServlet {


    public static final String UNIVERSITY_FILTER_NAME = "universities_filter_names[]";

    public FinishedReportServlet() {
        super();
        System.out.println("In FinishedReportServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In CompletionReportServlet.doGet()..");

        //TODO: automate this.
        Map<String, String> table_headers_html = new LinkedHashMap<>();
        table_headers_html.put(MappingValues.USER_COLUMN_FULLNAME, "Name");
        table_headers_html.put(MappingValues.USER_COLUMN_USERNAME, "Username");
        table_headers_html.put(MappingValues.USER_COLUMN_UNIVERSITY, "University");
        table_headers_html.put(MappingValues.USER_COLUMN_TAZKIRA_ID, "Tazkira ID");
        table_headers_html.put(MappingValues.USER_COLUMN_GENDER, "Gender");

        table_headers_html.put(MappingValues.USER_COLUMN_ALL_COMPLETED,
                MappingValues.custom_fields_label.get(MappingValues.USER_COLUMN_ALL_COMPLETED));
        table_headers_html.put(MappingValues.USER_COLUMN_POST_TEST_COMPLETED,
            MappingValues.custom_fields_label.get(MappingValues.USER_COLUMN_POST_TEST_COMPLETED));

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
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doHead(req, resp);
    }

    public boolean agentModulePresentInStatement(XapiAgent agent, String verb,
                                                 List<String> activities, Object dbContext){
        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        XapiStatementManager statementManager = pm.getManager(XapiStatementManager.class);

        if(agent == null){
            return false;
        }

        Iterator<String> activitiesIterator = activities.iterator();
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

    /**
     * Make statement . Sends the statement to endpoint.
     * @param fromUser
     * @param forUser
     * @param statement
     */
    public void makeStatement(User fromUser, User forUser, JSONObject statement){

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In FinishedReportServlet.doPost()..");
        Object dbContext =
                getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

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

                boolean allCompleted = true;
                boolean postTestCompleted = false;

                /* University Filter */
                String user_university = ucfManager.getUserField(
                        user, MappingValues.custom_fields_map.get("university"), dbContext);

                boolean iWantToBreakFree =
                        ServletUtil.shouldIShowThisUserWithFilter(user_university,
                                allChoosenUniNames);
                if(iWantToBreakFree){
                    continue;
                }


                for(Module everyModule:MappingValues.ALL_MODULES){
                    boolean completed = agentModulePresentInStatement(agent,
                            MappingValues.XAPI_PASSED_VERB, everyModule.getIds(), dbContext);
                    if(!completed){
                        allCompleted = false;
                    }
                    userInfoJSON.put(everyModule.getShortID(), String.valueOf(completed));
                }

                if(allCompleted){
                    userInfoJSON.put(MappingValues.USER_COLUMN_ALL_COMPLETED, "true");
                }else{
                    userInfoJSON.put(MappingValues.USER_COLUMN_ALL_COMPLETED, "false");
                }

                if(postTestCompleted){
                    userInfoJSON.put(MappingValues.USER_COLUMN_POST_TEST_COMPLETED, "true");
                }else{
                    userInfoJSON.put(MappingValues.USER_COLUMN_POST_TEST_COMPLETED, "false");

                }

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
