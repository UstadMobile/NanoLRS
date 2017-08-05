package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class EnrollmentReportServlet extends HttpServlet {

    public EnrollmentReportServlet() {
        super();
        System.out.println("In EnrollmentReportView()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In EnrollmentReportView.doGet()..");
        Map<String, String> table_headers_html = new HashMap<String, String>();

        table_headers_html.put("username", "Username");
        table_headers_html.put("name", "Name");
        table_headers_html.put("university", "uid");
        table_headers_html.put("university_name", "University");
        table_headers_html.put("enrolled", "Status");

        request.setAttribute("table_headers_html",table_headers_html);
        request.setAttribute("static","/syncendpoint/");
        request.getRequestDispatcher("/reports/EnrollmentReport.jsp").forward(request, response);

        //response.sendRedirect("EnrollmentReport.jsp");
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
        System.out.println("In ErollmentReportServlet.doPOST()..");
        Object dbContext = getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);
        XapiStatementManager statementManager = pm.getManager(XapiStatementManager.class);
        XapiAgentManager agentManager = pm.getManager(XapiAgentManager.class);
        UserCustomFieldsManager ucfManager = pm.getManager(UserCustomFieldsManager.class);


        List<User> allUsers = null;
        String jsonToReturn = "";
        XapiAgent agent = null;
        List<User> enrolledUsers = new ArrayList<>();
        List<String> enrolledUsersString = new ArrayList<>();
        JSONArray userEnrollmentJSONArray = new JSONArray();

        try {
            allUsers = userManager.getAllEntities(dbContext);
            if(allUsers != null && !allUsers.isEmpty()){
                for(User user:allUsers){
                    List<XapiAgent> allAgents = agentManager.findByUser(dbContext, user);
                    if(allAgents!= null && !allAgents.isEmpty()){
                        agent = allAgents.get(0);
                    }else{
                        continue;
                    }
                    List allStatements = statementManager.findByParams(dbContext, null, null, agent,
                            null, null, null, false, false, -1, -1, 0);

                    if(allStatements != null && !allStatements.isEmpty()){
                        enrolledUsers.add(user);
                    }
                }
            }

            Map<String, Integer> uni_map = new HashMap<>();
            uni_map.put("Kabul University", 23);
            uni_map.put("Kabul Polytechnic University", 24);
            uni_map.put("Kabul Education University", 25);

            Map<String, Integer> custom_fields_map = new HashMap<>();
            custom_fields_map.put("university", 980);
            custom_fields_map.put("fullname", 981);
            custom_fields_map.put("gender", 983);
            custom_fields_map.put("email",982);
            custom_fields_map.put("phonenumber",984);
            custom_fields_map.put("faculty",985);

            String[] uni_names = req.getParameterValues("university_names[]");
            ArrayList allChoosenUniNames = new ArrayList();
            if(uni_names != null) {
                if(uni_names.length > 0){
                    for (int k = 0; k < uni_names.length; k++) {
                        String choosenUniName = uni_names[k];
                        allChoosenUniNames.add(choosenUniName);
                    }
                }
            }


            for(User user:allUsers){
                JSONObject userInfoJSON = new JSONObject();
                userInfoJSON.put("username", user.getUsername() );
                userInfoJSON.put("fullname", ucfManager.getUserField(user, custom_fields_map.get("fullname"), dbContext) ); //TODO: Put these somewhere
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


                if (enrolledUsers.contains(user)) {
                    userInfoJSON.put("enrolled", "true");
                }else{
                    userInfoJSON.put("enrolled", "false");
                }
                userEnrollmentJSONArray.put(userInfoJSON);
            }

            jsonToReturn = userEnrollmentJSONArray.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION!");
        }

        if(userEnrollmentJSONArray.length() < 1){
            jsonToReturn = "[\n" +
                    //"    \"data\": [\n" +
                    "        {\"username\": \"user1\", \"fullname\": \"Chai Zakir\", \"university\" : 23, \"university_name\": \"Kabul University\", \"enrolled\": \"true\"},\n" +
                    "        {\"username\": \"user2\", \"fullname\": \"Kafi Ismail\", \"university\" : 24, \"university_name\": \"AFG University\", \"enrolled\": \"false\"},\n" +
                    "        {\"username\": \"user3\", \"fullname\": \"Saeb Salik\", \"university\" : 24, \"university_name\": \"AFG University\", \"enrolled\": \"true\"},\n" +
                    "        {\"username\": \"user4\", \"fullname\": \"Aam Ali\", \"university\" : 23, \"university_name\": \"Kabul University\", \"enrolled\": \"false\"},\n" +
                    "        {\"username\": \"user5\", \"fullname\": \"Kafi Ismail\", \"university\" : 24, \"university_name\": \"AFG University\", \"enrolled\": \"false\"}\n" +
                    //"        ]\n" +
                    "    ]";
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
