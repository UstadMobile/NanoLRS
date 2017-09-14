package com.ustadmobile.nanolrs.servlet;

import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiState;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
 * Created by varuna on 9/10/2017.
 */

public class CourseUsageReportServlet  extends HttpServlet {


    public static final String UNIVERSITY_FILTER_NAME = "universities_filter_names[]";

    public CourseUsageReportServlet() {
        super();
        System.out.println("In CourseUsageReportServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Add moduleID to qestion map so we can look it up. return it as new questionMap
     * @param moduleID
     * @param questionMap
     */
    public static Map<String, String> appendModuleIdtoQuestionMap(String moduleID, Map<String, String> questionMap){
        Map<String, String> questionWithModuleIDMap = new LinkedHashMap<>();
        Iterator<Map.Entry<String, String>> questionMapIterator = questionMap.entrySet().iterator();
        while(questionMapIterator.hasNext()){
            Map.Entry<String, String> entry = questionMapIterator.next();
            questionWithModuleIDMap.put(moduleID.trim() + '/' + entry.getKey(),
                    ServletUtil.stringToHTMLString("Q: " + entry.getValue())
            );
            questionWithModuleIDMap.put(moduleID.trim() + '/' + entry.getKey() + MappingValues.MODULE_DURATION_BIT,
                    "(" + MappingValues.MODULE_DURATION_STRING + ")");
        }
        return questionWithModuleIDMap;
    }

    /**
     * Adds element of one into another and returns the another.
     * @param fillMe
     * @param pourMe
     * @return
     */
    public Map<String, String> putThisInThis(Map<String, String> fillMe, Map<String, String> pourMe){
        Iterator<Map.Entry<String, String>> pourIterator = pourMe.entrySet().iterator();
        while(pourIterator.hasNext()){
            Map.Entry<String, String> pourThis = pourIterator.next();
            fillMe.put(pourThis.getKey(), pourThis.getValue());
        }
        return fillMe;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In CourseUsageReportServlet.doGet()..");

        //TODO: automate this.
        Map<String, String> table_headers_html = new LinkedHashMap<>();
        //table_headers_html.put(MappingValues.USER_COLUMN_FULLNAME, "Name");
        table_headers_html.put(MappingValues.USER_COLUMN_USERNAME, "Username");
        //table_headers_html.put(MappingValues.USER_COLUMN_UNIVERSITY, "University");
        //table_headers_html.put(MappingValues.USER_COLUMN_TAZKIRA_ID, "Tazkira ID");
        //table_headers_html.put(MappingValues.USER_COLUMN_GENDER, "Gender");
        table_headers_html.put("blankspace", " ");

        int moduleIterator = 0;
        for(Module everyModule: MappingValues.ALL_MODULES){
            if(everyModule.getIds().size() > 0) {
                moduleIterator = moduleIterator + 1;
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_RESULT_BIT,
                        "Module " + moduleIterator + ": " + everyModule.getName() + " " + MappingValues.MODULE_RESULT_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_SCORE_BIT,
                        MappingValues.MODULE_SCORE_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                        MappingValues.MODULE_DURATION_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_ATTEMPT_BIT,
                        MappingValues.MODULE_ATTEMPT_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_REGISTRATION_BIT,
                        MappingValues.MODULE_REGISTRATION_STRING);
                table_headers_html = putThisInThis(table_headers_html,
                        appendModuleIdtoQuestionMap(everyModule.getIds().get(0),
                            everyModule.getQuestionMap())
                );
                table_headers_html.put("blankspace"," ");
            }
        }


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

                request.getRequestDispatcher("../CourseUsageReport.jsp").forward(request, response);
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

    /**
     * Find all statements by parameters.
     * @param agent
     * @param verb
     * @param activities
     * @param dbContext
     * @return
     */
    public static List<XapiStatement> findStatements(XapiAgent agent, String verb,
                                                     List<String> activities, String statementId,
                                                     String registration, Object dbContext){
        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        XapiStatementManager statementManager = pm.getManager(XapiStatementManager.class);

        if(agent == null){
            return null;
        }

        Iterator<String> activitiesIterator = activities.iterator();
        List<XapiStatement> relevantStatements = new LinkedList<>();
        while(activitiesIterator.hasNext()){
            String activity = activitiesIterator.next();
            List<XapiStatement> stmts = (List<XapiStatement>) statementManager.findByParams(
                    dbContext, statementId, null, agent, verb, activity, registration,
                    false, false, -1, -1, -1);

            for(XapiStatement st:stmts){
                relevantStatements.add(st);
            }
        }

        //relevantStatements.sort(Comparator.comparing(XapiStatement::getTimestamp()));
        Collections.sort(relevantStatements, new Comparator<XapiStatement>(){
            @Override
            public int compare(XapiStatement s1, XapiStatement s2){
                return Long.compare(s1.getTimestamp(), s2.getTimestamp());
            }
        });


        return relevantStatements;
    }

    /**
     * Checks if a verb is present for given agent in list of activities.
     * @param agent
     * @param verb
     * @param activities
     * @param dbContext
     * @return
     */
    public boolean agentModulePresentInStatement(XapiAgent agent,
                                                 String verb, List<String> activities,
                                                 Object dbContext){
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
     * Gets score for question ID and user for that reg ID.
     * @param questionId
     * @param agent
     * @param verb
     * @param registrationId
     * @param dbContext
     * @return
     */
    public static Map.Entry<String, Long> getScoreAndDuration(String questionId, XapiAgent agent,
                                              String verb, String registrationId, Object dbContext){
        Map<String, Long> scoreAndDurationMap = new HashMap<>();
        String score = "0";
        Long duration = 0L;
        //XapiAgent agent, String verb, List<String> activities, String statementId,
        // String registration, Object dbContext
        List<String> questionList = new LinkedList<>();
        questionList.add(questionId);
        List<XapiStatement> statements = findStatements(agent, verb, questionList, null,
                registrationId, dbContext);
        Iterator<XapiStatement> statementsIterator = statements.iterator();
        while(statementsIterator.hasNext()){
            XapiStatement statement = statementsIterator.next();
            score = String.valueOf(statement.getResultScoreScaled());
            duration = Long.valueOf(statement.getResultDuration());
            break;
        }

        scoreAndDurationMap.put(score, duration);
        Map.Entry<String, Long> scoreAndDuration = scoreAndDurationMap.entrySet().iterator().next();

        return scoreAndDuration;

    }

    /**
     * Gets score map for particular user and module for that reg ID.
     * @param agent
     * @param module
     * @param registrationId
     * @param dbContext
     * @return
     */
    public static Map<String, String> getScores(XapiAgent agent, Module module,
                                                String registrationId, Object dbContext){
        Map<String, String> scoreMap = new HashMap<>();
        Map<String, String> questions = module.getQuestionMap();
        Map<String, String> questionMap = appendModuleIdtoQuestionMap(module.getIds().get(0),
                module.getQuestionMap());
        Iterator<Map.Entry<String, String>> questionIterator = questionMap.entrySet().iterator();
        while(questionIterator.hasNext()){
            Map.Entry<String, String> entry = questionIterator.next();
            String questionScoreID = entry.getKey();
            String questionDurationID = questionScoreID + MappingValues.MODULE_DURATION_BIT;
            Map.Entry<String, Long> scoreAndDuration =
                    getScoreAndDuration(questionScoreID, agent, MappingValues.XAPI_ANSWERED_VERB,
                            registrationId, dbContext);
            String score = scoreAndDuration.getKey();
            Long duration = scoreAndDuration.getValue();
            if(duration == null){
                duration = 0L;
            }

            scoreMap.put(questionScoreID, score);
            scoreMap.put(questionDurationID, String.valueOf(duration));
        }
        return scoreMap;
    }

    /**
     * Gets all registration for a given user and module
     * @param agent
     * @param module
     * @param dbContext
     * @return
     */
    public static Map<String, Long> getAllRegistrations(XapiAgent agent, Module module, Object dbContext){
        /*
        Steps:
        1. Get all launched statements
        2. Get registrations

         */
        System.out.println("Checking allRegistrations for moduele: " + module.getName());
        List<String> allRegistrations = new LinkedList<>();
        Map<String, Long> allRegistrationsMap = new LinkedHashMap<>();
        List<XapiStatement> userStatements = findStatements(agent,MappingValues.XAPI_LAUNCHED_VERB,
                module.getIds(), null, null, dbContext);
        if(userStatements == null){
            //return allRegistrations;
            return allRegistrationsMap;
        }
        for(XapiStatement statement : userStatements){
            if(statement.getContextRegistration() != null &&
                    !statement.getContextRegistration().trim().equals("")){
                allRegistrations.add(statement.getContextRegistration());
                allRegistrationsMap.put(statement.getContextRegistration(), statement.getTimestamp());
            }
        }
        //return allRegistrations;
        return allRegistrationsMap;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In CourseUsageReportServlet.doPost()..");
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

                //Get agent:
                List<XapiAgent> agents = agentManager.findByUser(dbContext, user);
                if(agents != null && !agents.isEmpty()){
                    agent = agents.get(0);
                }else{
                    agent = null;
                }

                JSONObject userInfoJSON = ServletUtil.getUserInfo(user, dbContext);
                userInfoJSON.put("blankspace", "");

                /* University Filter */
                String user_university = ucfManager.getUserField(
                        user, MappingValues.custom_fields_map.get("university"), dbContext);

                boolean iWantToBreakFree =
                        ServletUtil.shouldIShowThisUserWithFilter(user_university,
                                allChoosenUniNames);
                if(iWantToBreakFree){
                    continue;
                }

                Map<String, JSONObject> userAttempt = new HashMap<>();

                for(Module everyModule:MappingValues.ALL_MODULES){

                    String moduleResult = "N/A";
                    String moduleScore = "";
                    Long moduleDuration = null;

                    //Get Result:
                    boolean moduleStarted = agentModulePresentInStatement(agent,
                            MappingValues.XAPI_LAUNCHED_VERB, everyModule.getIds(), dbContext);
                    boolean modulePassed = false;
                    if(moduleStarted){
                        modulePassed = agentModulePresentInStatement(agent,
                                MappingValues.XAPI_PASSED_VERB, everyModule.getIds(), dbContext);
                        if(modulePassed){
                            moduleResult = "PASSED";
                        }else{
                            moduleResult = "FAILED";
                        }
                    }
                    userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_RESULT_BIT,
                            moduleResult);



                    //Get all registrations and questions for each
                    //List<String> allRegistrations =
                    //        getAllRegistrations(agent, everyModule, dbContext);
                    Map<String, Long> allRegistrationsMap =
                            getAllRegistrations(agent, everyModule, dbContext);

                    int regIteration = 0;
                    boolean gotLatest = false;
                    Iterator<Map.Entry<String, Long>> allRegIterator = allRegistrationsMap.entrySet().iterator();
                    while(allRegIterator.hasNext()){
                        Map.Entry<String, Long> regEntry = allRegIterator.next();
                        String registrationId = regEntry.getKey();
                        Long registrationTime = regEntry.getValue();
                        Date registrationDate = new Date(registrationTime);
                        DateFormat df = new SimpleDateFormat("dd MMMMM yyyy HH:mm:ss");
                        String registrationDateString = df.format(registrationDate);
                        Long regTotalDuration = 0L;
                        regIteration = regIteration + 1;
                        JSONObject userRegEntry = new JSONObject();
                        userRegEntry.put("blankspace", "");
                        //Get score for every question:
                        Map<String, String> scoreMap = getScores(agent, everyModule,
                                registrationId, dbContext);
                        Iterator<Map.Entry<String, String>> scoreMapIterator =
                                scoreMap.entrySet().iterator();
                        while(scoreMapIterator.hasNext()){
                            Map.Entry<String, String> entry = scoreMapIterator.next();
                            userRegEntry.put(entry.getKey(), entry.getValue());
                            if(entry.getKey().endsWith(MappingValues.MODULE_DURATION_BIT)){
                                Long thisDuration = Long.parseLong(entry.getValue());
                                regTotalDuration = regTotalDuration + thisDuration;
                            }


                        }
                        userRegEntry.put(everyModule.getShortID() + MappingValues.MODULE_ATTEMPT_BIT,
                                registrationDateString);
                        userRegEntry.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                                String.valueOf(regTotalDuration));

                        userAttempt.put("r"+registrationId, userRegEntry);

                        if(!gotLatest){
                            gotLatest = true;
                            moduleDuration = regTotalDuration;
                            //XapiAgent agent, String verb, List<String> activities,
                            // String statementId, String registration, Object dbContext
                            List<XapiStatement> passedStatements =
                                    findStatements(agent, MappingValues.XAPI_PASSED_VERB,
                                            everyModule.getIds(), null, registrationId, dbContext);
                            List<XapiStatement> failedStatements =
                                    findStatements(agent, MappingValues.XAPI_FAILED_VERB,
                                            everyModule.getIds(), null, registrationId, dbContext);
                            if(!passedStatements.isEmpty()){
                                moduleScore = String.valueOf(
                                        passedStatements.get(0).getResultScoreScaled() * 100) + "%";
                            }else if(!failedStatements.isEmpty()){
                                moduleScore = String.valueOf(
                                        failedStatements.get(0).getResultScoreScaled() * 100) + "%";
                            }else{
                                moduleScore = "-";
                            }
                        }

                    }

                    //Get duration`
                    if(moduleDuration == null){
                        userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                                "");
                    }else{
                        userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                                String.valueOf(moduleDuration));
                    }

                    //Get score
                    if(moduleScore == null){
                        moduleScore = "";
                    }
                    userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_SCORE_BIT,
                            moduleScore);
                }

                userEnrollmentJSONArray.put(userInfoJSON);

                Iterator<Map.Entry<String, JSONObject>> attemptIteratory =
                        userAttempt.entrySet().iterator();
                while(attemptIteratory.hasNext()){
                    Map.Entry<String, JSONObject> attemptEntry = attemptIteratory.next();

                    userEnrollmentJSONArray.put(attemptEntry.getValue());
                }


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
