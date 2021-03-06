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
import java.util.Set;
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

public class CourseUsageReportServlet  extends HttpServlet {


    public static final String UNIVERSITY_FILTER_NAME = "universities_filter_names[]";
    public static final String MODULES_FILTER_NAME = "modules_filter_names[]";
    public static final String LEGACY_MODE_FILTER_NAME = "legacy_mode";
    public static final String MODULES_ONLY_NAME = "modules_only_mode";
    public static final String PASSED_STRING = "PASSED";
    public static final String FAILED_STRING = "FAILED";
    public static final String NA_STRING = "N/A";

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
    public static Map.Entry<Map, Map> appendModuleIdtoQuestionMap(String moduleID,
                                                                  Map<String, String> questionMap){
        Map<String, String> questionWithModuleIDMap = new LinkedHashMap<>();
        Map<String, String> questionWithModuleIDMapClass = new LinkedHashMap<>();
        Map<Map, Map> returnThis = new LinkedHashMap<>();

        Iterator<Map.Entry<String, String>> questionMapIterator = questionMap.entrySet().iterator();
        while(questionMapIterator.hasNext()){
            Map.Entry<String, String> entry = questionMapIterator.next();
            questionWithModuleIDMap.put(moduleID.trim() + '/' + entry.getKey(),
                    ServletUtil.stringToHTMLString("Q: " + entry.getValue())
            );
            questionWithModuleIDMapClass.put(moduleID.trim() + '/' + entry.getKey(),
                    "question");

            questionWithModuleIDMap.put(moduleID.trim() + '/' + entry.getKey() +
                            MappingValues.MODULE_DURATION_BIT,
                    "(" + MappingValues.MODULE_DURATION_STRING + ")");
            questionWithModuleIDMapClass.put(moduleID.trim() + '/' + entry.getKey() +
                            MappingValues.MODULE_DURATION_BIT,
                    "question_duration");

            questionWithModuleIDMap.put(moduleID.trim() + '/' +
                            entry.getKey() + MappingValues.MODULE_NO_ATTEMPTS_BIT,
                    "(" + MappingValues.MODULE_NO_ATTEMPTS_STRING + ")");
            questionWithModuleIDMapClass.put(moduleID.trim() + '/' +
                            entry.getKey() + MappingValues.MODULE_NO_ATTEMPTS_BIT,
                    "question_attempts");
        }

        //return questionWithModuleIDMap;
        returnThis.put(questionWithModuleIDMap, questionWithModuleIDMapClass);
        Map.Entry<Map, Map> entry = returnThis.entrySet().iterator().next();

        return entry;

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
        Map<String, String> table_headers_class = new LinkedHashMap<>();
        Map<String, String> table_headers_class2 = new LinkedHashMap<>();

        Map<String, String> table_headers_html2 = new LinkedHashMap<>();
        table_headers_html.put(MappingValues.USER_COLUMN_FULLNAME, "Name");
        table_headers_html2.put(MappingValues.USER_COLUMN_FULLNAME, "");
        table_headers_class.put(MappingValues.USER_COLUMN_FULLNAME, MappingValues.STICKY_CLASS_HTML);

        table_headers_html.put(MappingValues.USER_COLUMN_USERNAME, "Username");
        table_headers_html2.put(MappingValues.USER_COLUMN_USERNAME, "");
        table_headers_class.put(MappingValues.USER_COLUMN_USERNAME, MappingValues.STICKY_CLASS_HTML);

        //table_headers_html.put(MappingValues.USER_COLUMN_UNIVERSITY, "University");
        //table_headers_html2.put(MappingValues.USER_COLUMN_UNIVERSITY, "");
        //table_headers_class.put(MappingValues.USER_COLUMN_UNIVERSITY, MappingValues.STICKY_CLASS_HTML);

        table_headers_html.put(MappingValues.USER_COLUMN_TAZKIRA_ID, "Tazkira ID");
        table_headers_html2.put(MappingValues.USER_COLUMN_TAZKIRA_ID, "");

        table_headers_html.put(MappingValues.USER_COLUMN_GENDER, "Gender");
        table_headers_html2.put(MappingValues.USER_COLUMN_GENDER, "");

        table_headers_html.put("blankspace", " ");
        table_headers_html2.put("blankspace", " ");


        int moduleIterator = 0;
        for(Module everyModule: MappingValues.ALL_MODULES){
            if(everyModule.getIds().size() > 0) {
                moduleIterator = moduleIterator + 1;
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_RESULT_BIT,
                        "Module " + moduleIterator + ": " + everyModule.getName() + " " +
                                MappingValues.MODULE_RESULT_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_SCORE_BIT,
                        MappingValues.MODULE_SCORE_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                        MappingValues.MODULE_DURATION_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_ATTEMPT_BIT,
                        MappingValues.MODULE_ATTEMPT_STRING);
                table_headers_html.put(everyModule.getShortID() + MappingValues.MODULE_REGISTRATION_BIT,
                        MappingValues.MODULE_REGISTRATION_STRING);

                Map.Entry<Map, Map> questionMapWithClass = appendModuleIdtoQuestionMap(everyModule.getIds().get(0),
                        everyModule.getQuestionMap());
                Map<String, String> questionMap = questionMapWithClass.getKey();
                Map<String, String> questionMapClass = questionMapWithClass.getValue();

                Map<String, String> questionMapJustQuestion = new LinkedHashMap<>();

                Map<String, String> questionMapWithoutQuestionText = new LinkedHashMap<>();
                Iterator<Map.Entry<String, String>> questionMapIterator = questionMap.entrySet().iterator();
                while(questionMapIterator.hasNext()){
                    Map.Entry<String, String> entry = questionMapIterator.next();
                    if(entry.getValue().startsWith("Q: ")){
                        questionMapWithoutQuestionText.put(entry.getKey(), "Score");
                        questionMapJustQuestion.put(entry.getKey(), entry.getValue());
                    }else{
                        questionMapWithoutQuestionText.put(entry.getKey(), entry.getValue());
                        //questionMapJustQuestion.put(entry.getKey(), "");
                    }
                }
                //table_headers_html = putThisInThis(table_headers_html, questionMap);
                table_headers_html = putThisInThis(table_headers_html, questionMapJustQuestion);
                table_headers_class2 = putThisInThis(table_headers_class2, questionMapClass);
                table_headers_html.put("blankspace"," ");

                table_headers_html2.put(everyModule.getShortID() + MappingValues.MODULE_RESULT_BIT,
                        "");
                table_headers_html2.put(everyModule.getShortID() + MappingValues.MODULE_SCORE_BIT,
                        "");
                table_headers_html2.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                        "");
                table_headers_html2.put(everyModule.getShortID() + MappingValues.MODULE_ATTEMPT_BIT,
                        "");
                table_headers_html2.put(everyModule.getShortID() + MappingValues.MODULE_REGISTRATION_BIT,
                        "");

                table_headers_html2 = putThisInThis(table_headers_html2,questionMapWithoutQuestionText);
                table_headers_html2.put("blankspace", " ");
            }
        }


        HttpSession session=request.getSession();
        String sessionAdmin = (String)session.getAttribute(MappingValues.SUPER_ADMIN_USERNAME);
        if(sessionAdmin != null){
            if(sessionAdmin.equals(MappingValues.SUPER_ADMIN_USERNAME)){
                request.setAttribute("table_headers_html",table_headers_html);
                request.setAttribute("table_headers_html2",table_headers_html2);
                request.setAttribute("table_headers_class2",table_headers_class2);
                request.setAttribute("static","/syncendpoint/");
                request.setAttribute("universities", MappingValues.universities);
                request.setAttribute("modules", MappingValues.ALL_MODULES);

                request.getSession().setAttribute("table_headers_html",table_headers_html);
                request.getSession().setAttribute("table_headers_html2",table_headers_html2);
                request.getSession().setAttribute("table_headers_class2",table_headers_class2);
                request.getSession().setAttribute("table_headers_class",table_headers_class);
                request.getSession().setAttribute("static","/syncendpoint/");
                request.getSession().setAttribute("universities", MappingValues.universities);
                request.getSession().setAttribute("modules", MappingValues.ALL_MODULES);

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
     * Checks if a verb is present for list of agents in list of activities.
     * @param agents
     * @param verb
     * @param activities
     * @param dbContext
     * @return
     */
    public boolean agentModulePresentInStatementAgents(List<XapiAgent> agents,
                                                 String verb, List<String> activities,
                                                 Object dbContext) {
        boolean result = false;
        for(XapiAgent agent: agents){
            boolean thisAgentResult = agentModulePresentInStatement(agent, verb, activities, dbContext);
            if(thisAgentResult == true){
                return true;
            }
        }
        return result;
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
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(days);
        sb.append(" d ");
        sb.append(hours);
        sb.append(" h ");
        sb.append(minutes);
        sb.append(" m ");
        sb.append(seconds);
        sb.append(" s");

        return(sb.toString());
    }

    /**
     * Get question details for qn id, agent in a particular registration.
     * @param questionId
     * @param agent
     * @param registrationId
     * @param dbContext
     * @return
     */
    public static Map<String, String> getQuestionResult(String questionId, XapiAgent agent,
                                                        String registrationId, Object dbContext){

        float score = 0;
        Long duration = 0L;
        int noAttempts;

        String agentUUID="";
        if(agent != null){
            agentUUID=agent.getUuid();
        }
        //System.out.println("Qid|agent|regId" + questionId +"|"+ agentUUID +"|"+ registrationId);

        Map<String, String> questionResultMap = new LinkedHashMap<>();

        List<String> questionList = new LinkedList<>();
        questionList.add(questionId);
        List<XapiStatement> statements = findStatements(agent, MappingValues.XAPI_ANSWERED_VERB,
                questionList, null, registrationId, dbContext);
        Iterator<XapiStatement> statementsIterator = statements.iterator();
        while(statementsIterator.hasNext()){
            XapiStatement statement = statementsIterator.next();
            if(statement.getResultScoreScaled() != 0){
                score = statement.getResultScoreScaled();
            }
            duration = duration + statement.getResultDuration();
        }
        noAttempts = statements.size();
        Long durationSeconds = duration/1000;
        questionResultMap.put(questionId, String.valueOf(score));
        questionResultMap.put(questionId + MappingValues.MODULE_DURATION_BIT, String.valueOf(durationSeconds));
        questionResultMap.put(questionId + MappingValues.MODULE_NO_ATTEMPTS_BIT, String.valueOf(noAttempts));

        return questionResultMap;
    }

    /**
     * Gets score map for list of agents (same user) and module for that reg ID.
     * @param agents
     * @param module
     * @param registrationId
     * @param dbContext
     * @return
     */
    public static Map<String, String> getScoresForAgents(List<XapiAgent> agents, Module module,
                                                String registrationId, Object dbContext){
        Map<String, String> scoreMap = new LinkedHashMap<>();
        for(XapiAgent agent:agents){
            Map<String, String> thisAgentScoreMap = new LinkedHashMap<>();
            thisAgentScoreMap = getScores(agent, module, registrationId, dbContext);
            Iterator<Map.Entry<String, String>> resultIterator =
                    thisAgentScoreMap.entrySet().iterator();
            while(resultIterator.hasNext()){
                Map.Entry<String, String> agentResult = resultIterator.next();
                scoreMap.put(agentResult.getKey(), agentResult.getValue());
            }
        }
        return scoreMap;

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

        Iterator<Map.Entry<String, String>> questionIterator = questions.entrySet().iterator();
        //For every question:
        while(questionIterator.hasNext()){
            Map.Entry<String, String> entry = questionIterator.next();
            String questionID = module.getIds().get(0).trim() + '/' + entry.getKey();

            Map<String, String> questionResultMap =
                    getQuestionResult(questionID, agent, registrationId, dbContext);
            Iterator<Map.Entry<String, String>> questionResultIterator = questionResultMap.entrySet().iterator();
            while(questionResultIterator.hasNext()){
                Map.Entry<String, String> questionResult = questionResultIterator.next();
                scoreMap.put(questionResult.getKey(), questionResult.getValue());
            }

        }
        return scoreMap;
    }

    /**
     * Default legacy mode to false
     * @param agent
     * @param module
     * @param dbContext
     * @return
     */
    public static Map<String, Long> getAllRegistrations(XapiAgent agent, Module module,Object dbContext){
        return getAllRegistrations(agent, module, false, dbContext);
    }

    /**
     * Default legacy mode to false
     * @param agents
     * @param module
     * @param dbContext
     * @return
     */
    public static Map<String, Long> getAllRegistrationsForAgents(List<XapiAgent> agents,
                                                                 Module module, Object dbContext){
        return getAllRegistrationsForAgents(agents, module, false, dbContext);
    }

    /**
     * Gets all registration for a given user's agent list and module
     * @param agents
     * @param module
     * @param dbContext
     * @return
     */
    public static Map<String, Long> getAllRegistrationsForAgents(List<XapiAgent> agents,
                                     Module module, boolean legacyMode, Object dbContext){

        Map<String, Long> allRegistrations = new LinkedHashMap<>();
        for(XapiAgent agent:agents){
            Map<String, Long> agentRegistrations =
                    getAllRegistrations(agent, module, legacyMode, dbContext);
            Iterator<Map.Entry<String, Long>> regIterator = agentRegistrations.entrySet().iterator();
            while(regIterator.hasNext()){
                Map.Entry<String, Long> entry = regIterator.next();
                allRegistrations.put(entry.getKey(), entry.getValue());
            }
        }
        return allRegistrations;
    }


    /**
     * Gets all registration for a given user and module
     * @param agent
     * @param module
     * @param dbContext
     * @return
     */
    public static Map<String, Long> getAllRegistrations(XapiAgent agent, Module module,
                                                        boolean legacyMode, Object dbContext){

        //System.out.println("Checking allRegistrations for moduele: " + module.getName());
        List<String> allRegistrations = new LinkedList<>();
        List<XapiStatement> userStatements;
        Map<String, Long> allRegistrationsMap = new LinkedHashMap<>();
        if(legacyMode) {
            userStatements = findStatements(agent, MappingValues.XAPI_LAUNCHED_VERB,
                    module.getIds(), null, null, dbContext);
        }else {
            userStatements = findStatements(agent, MappingValues.XAPI_INITIALIZED_VERB,
                    module.getIds(), null, null, dbContext);
        }
        if(userStatements == null){
            /*
            if(agent == null){
                System.out.println("No launched statement for  (unknown agent)");
            }else {
                System.out.println("No launched statements for: " + agent.getUuid());
            }
            */
            return allRegistrationsMap;
        }
        for(XapiStatement statement : userStatements){
            if(statement.getContextRegistration() != null &&
                    !statement.getContextRegistration().trim().equals("")){
                allRegistrations.add(statement.getContextRegistration());
                allRegistrationsMap.put(statement.getContextRegistration(), statement.getTimestamp());
            }
        }
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
        String[] mod_names = req.getParameterValues(MODULES_FILTER_NAME);
        String legacyMode = req.getParameter(LEGACY_MODE_FILTER_NAME);
        String modulesOnly = req.getParameter(MODULES_ONLY_NAME);

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

            //Modules filter
            ArrayList allChoosenModuleNames = new ArrayList();
            if(mod_names != null){
                if(mod_names.length >0){
                    for(int m=0; m < mod_names.length; m++){
                        String choosenModName = mod_names[m];
                        allChoosenModuleNames.add(choosenModName);
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
                    //System.out.println("Agent is null for : " + user.getUsername());
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

                Map<String, JSONObject> userAttempt = new LinkedHashMap<>();

                for(Module everyModule:MappingValues.ALL_MODULES){

                    //Modules filter
                    if(allChoosenModuleNames.contains(everyModule.getShortID())){
                        continue;
                    }

                    String moduleResult = NA_STRING;
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
                            moduleResult = PASSED_STRING;
                        }else{
                            moduleResult = FAILED_STRING;
                        }
                    }
                    userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_RESULT_BIT,
                            moduleResult);


                    boolean legacyModeBoolean = false;
                    if(legacyMode != null && !legacyMode.isEmpty()){
                        if(legacyMode.equals("true")) {
                            legacyMode = "true";
                            legacyModeBoolean = true;
                        }
                    }
                    boolean modulesOnlyBoolean = false;
                    if(modulesOnly != null && !modulesOnly.isEmpty()){
                        if(modulesOnly.equals("true")){
                            modulesOnly = "true";
                            modulesOnlyBoolean = true;
                        }
                    }

                    Map<String, Long> allRegistrationsMap =
                            getAllRegistrations(agent, everyModule, legacyModeBoolean, dbContext);

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
                        String regTotalScore;
                        JSONObject userRegEntry = new JSONObject();
                        //userRegEntry.put("blankspace", "");
                        //userRegEntry.put(MappingValues.USER_COLUMN_USERNAME, "");
                        userRegEntry.put("blankspace","blankspace-UM:" + user.getUsername());

                        //Get total duration, score, no. of attempts for every qn in this module
                        Map<String, String> scoreMap = getScores(agent, everyModule,
                                registrationId, dbContext);
                        Iterator<Map.Entry<String, String>> scoreMapIterator =
                                scoreMap.entrySet().iterator();
                        String appendThis="";
                        while(scoreMapIterator.hasNext()){
                            Map.Entry<String, String> entry = scoreMapIterator.next();
                            //Add values to this reg entry
                            appendThis = "";
                            if(entry.getKey().endsWith(MappingValues.MODULE_DURATION_BIT)){
                                appendThis = "s";
                            }
                            if(entry.getKey().endsWith(MappingValues.MODULE_SCORE_BIT)){
                                //appendThis = "%";
                            }
                            userRegEntry.put(entry.getKey(), entry.getValue() + appendThis);

                            //Update total duration for this registration
                            if(entry.getKey().endsWith(MappingValues.MODULE_DURATION_BIT)){
                                //System.out.println("Parsing this: " + entry.getKey() + " " + entry.getValue());
                                Long thisDuration = Long.parseLong(entry.getValue());
                                regTotalDuration = regTotalDuration + thisDuration;
                            }

                        }

                        List<XapiStatement> regPassed =
                                findStatements(agent, MappingValues.XAPI_PASSED_VERB,
                                        everyModule.getIds(), null, registrationId, dbContext);
                        List<XapiStatement> regFailed =
                                findStatements(agent, MappingValues.XAPI_FAILED_VERB,
                                        everyModule.getIds(), null, registrationId, dbContext);
                        if(!regPassed.isEmpty()){
                            regTotalScore = String.valueOf(
                                    regPassed.get(0).getResultScoreScaled() * 100) + "%";
                        }else if(!regFailed.isEmpty()){
                            regTotalScore = String.valueOf(
                                    regFailed.get(0).getResultScoreScaled() * 100) + "%";
                        }else{
                            regTotalScore = "-";
                        }
                        userRegEntry.put(everyModule.getShortID() + MappingValues.MODULE_ATTEMPT_BIT,
                                registrationDateString);
                        String regTotalDurationReadable = getDurationBreakdown(regTotalDuration*1000);
                        //userRegEntry.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                        //        String.valueOf(regTotalDuration));
                        userRegEntry.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                                String.valueOf(regTotalDurationReadable));

                        userRegEntry.put(everyModule.getShortID() + MappingValues.MODULE_SCORE_BIT,
                                regTotalScore);

                        userRegEntry.put(everyModule.getShortID() + MappingValues.MODULE_REGISTRATION_BIT,
                                registrationId);

                        if(userAttempt.get("r"+regIteration) != null){
                            JSONObject existingEntry = userAttempt.get("r" + regIteration);
                            for(String key:JSONObject.getNames(userRegEntry)){
                                if(!existingEntry.isNull(key)){
                                    System.out.println("WARNING > VALUE: " + key + " ALREADY EXIST!");
                                }
                                existingEntry.put(key, userRegEntry.get(key));
                            }
                            userAttempt.put("r"+regIteration, existingEntry);

                        }else{
                            userAttempt.put("r"+regIteration, userRegEntry);
                        }

                        if(!gotLatest){
                            gotLatest = true;
                            moduleDuration = regTotalDuration;
                            moduleScore = regTotalScore;
                        }

                    } //end of reg loop

                    if(allRegistrationsMap.size() > 0 ) {
                        userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_RESULT_BIT,
                                moduleResult);
                    }else{
                        userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_RESULT_BIT,
                                NA_STRING);
                    }


                    //Get duration`
                    if(moduleDuration == null){
                        userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                                "");
                    }else{
                        String regTotalDurationReadable = getDurationBreakdown(moduleDuration*1000);
                        userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_DURATION_BIT,
                                String.valueOf(regTotalDurationReadable));
                    }

                    //Get score
                    if(moduleScore == null){
                        moduleScore = "";
                    }
                    userInfoJSON.put(everyModule.getShortID() + MappingValues.MODULE_SCORE_BIT,
                            moduleScore);

                }// end of module loop

                userEnrollmentJSONArray.put(userInfoJSON);

                Iterator<Map.Entry<String, JSONObject>> attemptIteratory = userAttempt.entrySet().iterator();
                while(attemptIteratory.hasNext()){
                    Map.Entry<String, JSONObject> attemptEntry = attemptIteratory.next();

                    userEnrollmentJSONArray.put(attemptEntry.getValue());

                }

            }//end of user loop

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
