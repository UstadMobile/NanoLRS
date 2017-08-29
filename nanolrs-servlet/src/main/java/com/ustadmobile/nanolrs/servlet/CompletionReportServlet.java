package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

        Map<String, String> table_headers_html = new HashMap<String, String>();

        table_headers_html.put("username", "Username");
        table_headers_html.put("name", "Name");
        table_headers_html.put("university", "uid");
        table_headers_html.put("university_name", "University");
        table_headers_html.put("enrolled", "Status");

        HttpSession session=request.getSession();
        String sessionAdmin = (String)session.getAttribute("admin");
        if(sessionAdmin != null){
            if(sessionAdmin.equals("admin")){
                request.setAttribute("table_headers_html",table_headers_html);
                request.setAttribute("static","/syncendpoint/");
                response.sendRedirect("../CompletionReport.jsp");
            }else{
                response.sendRedirect("../../Login.jsp");
            }
        }else {
            response.sendRedirect("../../Login.jsp");
        }



        /*
        request.setAttribute("table_headers_html",table_headers_html);
        request.setAttribute("static","/syncendpoint/");
        request.getRequestDispatcher("/reports/CompletionReport.jsp").forward(request, response);
        */
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
        XapiStatementManager statementManager = pm.getManager(XapiStatementManager.class);
        XapiAgentManager agentManager = pm.getManager(XapiAgentManager.class);
        UserCustomFieldsManager ucfManager = pm.getManager(UserCustomFieldsManager.class);

        String[] uni_names = req.getParameterValues("university_names[]");

        List<String> module1 = new ArrayList<String>();
        module1.add("epub:202b10fe-b028-4b84-9b84-852aa123456a");
        module1.add("epub:202b10fe-b028-4b84-9b84-852aa123456b");
        module1.add("epub:202b10fe-b028-4b84-9b84-852aa123456c");

        List<String> module2 = new ArrayList<String>();
        module2.add("epub:eb0476a2-b8b1-43e3-bb85-f0e51e143afe");
        module2.add("epub:023970e2-2d4b-4fd5-9bbd-de373bb2aad6");
        module2.add("epub:6f747783-9ec3-4195-a3c7-3c417efaf8ea");

        List<String> module3 = new ArrayList<String>();
        module3.add("epub:114f6e63-80f2-4d10-9a70-efa113eb9f65");
        module3.add("epub:6c27c4c0-6fc7-4c89-b8f8-54cd270666e9");
        module3.add("epub:16f526b1-90e4-4e3d-a0e9-05e73bba9953");

        List<String> module4 = new ArrayList<String>();
        module4.add("epub:3ce0e992-050c-4fbf-90c9-4dcb2b82bc64");
        module4.add("epub:e95ec3d7-d56b-4541-8d45-4684dfdf64a6");
        module4.add("epub:31e04e55-e29d-422f-9e99-c3f2fd1f6f4a");

        List<User> allUsers = null;
        String jsonToReturn = "";
        XapiAgent agent = null;
        List<User> completedUsers = new ArrayList<>();
        List<String> enrolledUsersString = new ArrayList<>();
        JSONArray userEnrollmentJSONArray = new JSONArray();

        try {
            allUsers = userManager.getAllEntities(dbContext);

            Map<String, String> uni_map = new HashMap<>();
            uni_map.put("Kabul University", "KU");
            uni_map.put("Kabul Polytechnic University", "KPU");
            uni_map.put("Kabul Education University", "KEU");

            Map<String, Integer> custom_fields_map = new HashMap<>();
            custom_fields_map.put("university", 980);
            custom_fields_map.put("fullname", 981);
            custom_fields_map.put("gender", 983);
            custom_fields_map.put("email",982);
            custom_fields_map.put("phonenumber",984);
            custom_fields_map.put("faculty",985);


            ArrayList allChoosenUniNames = new ArrayList();
            if(uni_names != null) {
                if(uni_names.length > 0){
                    for (int k = 0; k < uni_names.length; k++) {
                        String choosenUniName = uni_names[k];
                        allChoosenUniNames.add(choosenUniName);
                    }
                }
            }

            for(User user:allUsers) {
                String username = user.getUsername();
                if(username.equals("admin")){
                    //Don't show admins stuff
                    continue;
                }
                JSONObject userInfoJSON = new JSONObject();
                userInfoJSON.put("username", username);
                userInfoJSON.put("fullname", ucfManager.getUserField(user, custom_fields_map.get("fullname"), dbContext));
                String uni_name = ucfManager.getUserField(user, custom_fields_map.get("university"), dbContext);

                System.out.println("Should I skip?");
                boolean iWantToBreakFree = false;
                boolean showAll = false;
                System.out.println("Checking if: " + uni_name + " is in: " + uni_names);
                if (allChoosenUniNames.isEmpty()) {
                    //Let it go.. Let it go..
                }else if(allChoosenUniNames.contains("ALL")){
                    //Let it go, Let it go..
                }else if(!allChoosenUniNames.contains(uni_name)){
                    iWantToBreakFree = true;
                    System.out.println("YES");
                    continue;
                }
                System.out.println("NO");

                userInfoJSON.put("university_name", uni_name);
                if(uni_map.containsKey(uni_name)){
                    userInfoJSON.put("university", uni_map.get(uni_name));
                }else{
                    userInfoJSON.put("university", "");
                }

                List<XapiAgent> agents = agentManager.findByUser(dbContext, user);
                if(agents != null && !agents.isEmpty()){
                    agent = agents.get(0);
                }else{
                    agent = null;
                }

                String m1result= "";
                String m2result = "";
                String m3result = "";
                String m4result = "";

                boolean m1resultpass = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module1, dbContext);
                boolean m1resultfail = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module1, dbContext);
                if(m1resultpass){
                    m1result="true";
                }else if(m1resultfail){
                    m1result = "false";
                }


                boolean m2resultpass = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module2, dbContext);
                boolean m2resultfail = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module2, dbContext);
                if(m2resultpass){
                    m2result="true";
                }else if(m2resultfail){
                    m2result = "false";
                }

                boolean m3resultpass = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module3, dbContext);
                boolean m3resultfail = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module3, dbContext);
                if(m3resultpass){
                    m3result="true";
                }else if(m3resultfail){
                    m3result = "false";
                }

                boolean m4resultpass = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module4, dbContext);
                boolean m4resultfail = agentModulePresentInStatement(agent,
                        "http://adlnet.gov/expapi/verbs/passed", module4, dbContext);
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

        /*
        if(userEnrollmentJSONArray.length() < 1){
            jsonToReturn = "[\n" +
                    "        {\"username\": \"user1\", \"fullname\": \"Chai Zakir\", \"university\" : 23, \"university_name\": \"Kabul University\", \"m1\": \"true\", \"m2\": \"true\", \"m3\": \"true\", \"m4\": \"true\"},\n" +
                    "        {\"username\": \"user2\", \"fullname\": \"Kafi Ismail\", \"university\" : 24, \"university_name\": \"AFG University\", \"m1\": \"true\", \"m2\": \"false\", \"m3\": \"false\", \"m4\": \"false\"},\n" +
                    "        {\"username\": \"user3\", \"fullname\": \"Saeb Salik\", \"university\" : 24, \"university_name\": \"AFG University\", \"m1\": \"true\", \"m2\": \"true\", \"m3\": \"true\", \"m4\": \"true\"},\n" +
                    "        {\"username\": \"user4\", \"fullname\": \"Aam Ali\", \"university\" : 23, \"university_name\": \"Kabul University\", \"m1\": \"true\", \"m2\": \"true\", \"m3\": \"false\", \"m4\": \"false\"},\n" +
                    "        {\"username\": \"user5\", \"fullname\": \"Kafi Ismail\", \"university\" : 24, \"university_name\": \"AFG University\", \"m1\": \"false\", \"m2\": \"false\", \"m3\": \"false\", \"m4\": \"false\"}\n" +
                    "    ]";
        }
        */

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
