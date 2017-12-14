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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

public class SummaryReportServlet extends HttpServlet {

    public class SummaryResult{
        private List<User> enrolled;
        private List<User> active;
        private List<User> completed;
        private Map<String, Long> durationMap;

        public SummaryResult(List<User> enrolled, List<User> active, List<User> completed,
                             Map<String, Long> durationMap) {
            this.enrolled = enrolled;
            this.active = active;
            this.completed = completed;
            this.durationMap = durationMap;
        }

        public List<User> getEnrolled() {
            return enrolled;
        }

        public void setEnrolled(List<User> enrolled) {
            this.enrolled = enrolled;
        }

        public List<User> getActive() {
            return active;
        }

        public void setActive(List<User> active) {
            this.active = active;
        }

        public List<User> getCompleted() {
            return completed;
        }

        public void setCompleted(List<User> completed) {
            this.completed = completed;
        }

        public Map<String, Long> getDurationMap() {
            return durationMap;
        }

        public void setDurationMap(Map<String, Long> durationMap) {
            this.durationMap = durationMap;
        }
    }

    public static final String UNIVERSITY_FILTER_NAME = "universities_filter_names[]";
    public static final String LEGACY_MODE_FILTER_NAME = "legacy_mode";
    public static final String PASSED_STRING = "PASSED";
    public static final String FAILED_STRING = "FAILED";
    public static final String NA_STRING = "N/A";
    public static final int DEFAULT_DAYS_TO_LOOK_BACK = 7;

    public SummaryReportServlet() {
        super();
        System.out.println("In SummaryReportServlet()..");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("In SummaryReportServlet.doGet()..");

        Map<String, String> table_headers_html = new LinkedHashMap<>();
        Map<String, String> table_headers_class = new LinkedHashMap<>();

        table_headers_html.put(MappingValues.COLUMN_TITLE, "");
        table_headers_html.put(MappingValues.COLUMN_NO_FEMALES,
                MappingValues.custom_fields_label.get(MappingValues.COLUMN_NO_FEMALES));
        table_headers_html.put(MappingValues.COLUMN_NO_MALES,
                MappingValues.custom_fields_label.get(MappingValues.COLUMN_NO_MALES));
        table_headers_html.put(MappingValues.COLUMN_TOTAL,
                MappingValues.custom_fields_label.get(MappingValues.COLUMN_TOTAL));

        HttpSession session=request.getSession();
        String sessionAdmin = (String)session.getAttribute(MappingValues.SUPER_ADMIN_USERNAME);
        if(sessionAdmin != null){
            if(sessionAdmin.equals(MappingValues.SUPER_ADMIN_USERNAME)){
                request.setAttribute("table_headers_html",table_headers_html);
                request.setAttribute("static","/syncendpoint/");
                request.setAttribute("days",DEFAULT_DAYS_TO_LOOK_BACK);
                request.setAttribute("universities", MappingValues.universities);

                request.getSession().setAttribute("table_headers_html",table_headers_html);
                request.getSession().setAttribute("table_headers_class",table_headers_class);
                request.getSession().setAttribute("static","/syncendpoint/");
                request.getSession().setAttribute("days",DEFAULT_DAYS_TO_LOOK_BACK);
                request.getSession().setAttribute("universities", MappingValues.universities);

                request.getRequestDispatcher("../SummaryReport.jsp").forward(request, response);
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
     * Minus days from current date time in ms. Returns in ms as well.
     * @param days
     * @return
     */
    public long getDateBeforeDays(int days){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, - days);
        Date fromDateDate = cal.getTime();
        long pastDate = fromDateDate.getTime();
        return pastDate;
    }

    public long getDateFromString(String date){
        //12/01/2017 00:00 - Dec 1st 2017
        //"MM/dd/yyyy HH:mm"
        String format = "yyyy-MM-dd";
        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            //return 0;
        }
        return d.getTime();
    }

    /**
     * Get summary report result that has enrolled users, active users,
     * completed users and duration map.
     * @param fromDate
     * @param toDate
     * @param dbContext
     * @return
     * @throws SQLException
     */
    public SummaryResult getSummaryResult( long fromDate, long toDate, Object dbContext)
            throws SQLException{

        return getSummaryResult(null, fromDate, toDate, dbContext);
    }

    /**
     * Get summary report result that has enrolled users, active users,
     * completed users and duration map.
     * @param fromDate
     * @param toDate
     * @param dbContext
     * @return
     * @throws SQLException
     */
    public SummaryResult getSummaryResult(List<User> allUsers, long fromDate, long toDate,
                                          Object dbContext) throws SQLException{

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);
        XapiAgentManager agentManager = pm.getManager(XapiAgentManager.class);

        //Get all Users
        if(allUsers == null) {
            allUsers = userManager.getAllEntities(dbContext);
        }

        System.out.println("Getting list of enrolledUsers..");
        List<User> enrolledUsers = userManager.getAllSinceTwoDates(fromDate, toDate, dbContext);
        List<User> activeUsers = new LinkedList<>();
        List<User> completedUsers = new LinkedList<>();
        Map<String, Long> durationMap = new LinkedHashMap<>();

        System.out.println("SummaryResult: Main loop");
        for(User user: allUsers){
            //System.out.println("    Report Loop User: " + user.getUsername());
            XapiAgent agent;
            long duration = 0;
            //Get agent:
            //System.out.println("    Getting agent..");
            List<XapiAgent> agents = agentManager.findByUser(dbContext, user);
            if(agents != null && !agents.isEmpty()){
                agent = agents.get(0);
            }else{
                //System.out.println("    Agent is null for : " + user.getUsername());
                agent = null;
            }

            if(agent != null){
                //System.out.println("    statements..");
                List<XapiStatement> statements = getStatementsInTimeRange(user, agent, fromDate,
                        toDate, null, dbContext);
                if(statements.size() > 0){
                    activeUsers.add(user);
                }
                //System.out.println("    statementsCompleted..");
                List<XapiStatement> statementsCompleted = getStatementsInTimeRange(user, agent, fromDate,
                        toDate, MappingValues.XAPI_COMPLETED_VERB, dbContext);
                if(statementsCompleted.size() > 0){
                    completedUsers.add(user);
                }

                //System.out.println("    statementsDuration..");
                List<XapiStatement> statementsDuration =
                        getStatementsInTimeRange(user, agent, fromDate,
                                toDate, MappingValues.XAPI_ANSWERED_VERB, dbContext);
                //System.out.println("    getting duration..");
                for(XapiStatement statement: statementsDuration){
                    long thisDuration = statement.getResultDuration();
                    duration = duration + thisDuration;
                }
            }

            durationMap.put(user.getUsername(), duration);
            //System.out.println("    durationMap done.");
            //System.out.println("");

        }
        System.out.println("SummaryResult: end of main loop");
        System.out.println("");
        SummaryResult result = new SummaryResult(enrolledUsers, activeUsers, completedUsers,
                durationMap);
        return result;
    }


    /**
     * Get all statements in a time range
     * @param user
     * @param agent
     * @param fromDate
     * @param toDate
     * @param dbContext
     * @return
     * @throws SQLException
     */
    public List<XapiStatement> getStatementsInTimeRange(User user, XapiAgent agent, long fromDate,
                                                        long toDate, String verb, Object dbContext) throws SQLException {
        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        XapiStatementManager statementManager = pm.getManager(XapiStatementManager.class);

        List<? extends XapiStatement> statements = statementManager.findByParams(
                dbContext, null, null, agent, verb, null, null,
                false, false, fromDate, toDate,0);

        return (List<XapiStatement>) statements;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In SummaryReportServlet.doPost()..");
        Object dbContext =
                getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        //Managers:
        PersistenceManager pm = PersistenceManager.getInstance();
        UserManager userManager = pm.getManager(UserManager.class);
        UserCustomFieldsManager ucfManager = pm.getManager(UserCustomFieldsManager.class);

        List<User> allUsers = null;
        //The json to return
        JSONArray reportJSONArray = new JSONArray();
        String jsonToReturn = "";

        String[] uni_names = req.getParameterValues(UNIVERSITY_FILTER_NAME);

        try {
            allUsers = userManager.getAllEntities(dbContext);

            //UPDATE: Summary Report doesnt have a Uni Filter yet.
            /* uncomment and use after Uni filter in report implemented.
            //Get choosen uni filter (if any)
            ArrayList allChoosenUniNames = new ArrayList();
            if(uni_names != null) {
                if(uni_names.length > 0){
                    for (int k = 0; k < uni_names.length; k++) {
                        String choosenUniName = uni_names[k];
                        allChoosenUniNames.add(choosenUniName);
                    }
                }
            }
            */

            //Map of [University -> <Users>]
            Map<String, List<User>> userToUniMap = new LinkedHashMap<>();
            //Loop through all users to : Map Users to University
            for(User user:allUsers){

                //Don't show testing users
                if(user.getNotes() != null){
                    if(user.getNotes().equals("testing")) {
                        //Don't show testing users
                        continue;
                    }
                }

                //Get this user's university
                String userUniversity = ucfManager.getUserField(user,
                        MappingValues.custom_fields_map.get("university"), dbContext);

                //Put all Null/Empty Universities into "Other" category
                if(userUniversity == null || userUniversity.isEmpty()){
                    userUniversity = "Other";
                }

                //Put User into the Uni-><User> map.
                if(userToUniMap.containsKey(userUniversity)){
                    List<User> thisUniUsers = userToUniMap.get(userUniversity);
                    thisUniUsers.add(user);
                    userToUniMap.put(userUniversity, thisUniUsers);
                }else{
                    List<User> newUniUsers = new LinkedList<>();
                    newUniUsers.add(user);
                    userToUniMap.put(userUniversity, newUniUsers);
                }

            }//end of University-><Users> map generation.

            //Create enrollment JSON Objects
            JSONArray enrollmentRows = new JSONArray();
            JSONObject enrollmentRowHeader = new JSONObject();
            JSONObject enrollmentRowTotal = new JSONObject();

            //Create active JSON Objects
            JSONArray activeRows = new JSONArray();
            JSONObject activeRowHeader = new JSONObject();
            JSONObject activeRowTotal = new JSONObject();

            //Create completed JSON Objects
            JSONArray completedRows = new JSONArray();
            JSONObject completedRowHeader = new JSONObject();
            JSONObject completedRowTotal = new JSONObject();

            //Create duration JSON Objects
            JSONArray timeRows = new JSONArray();
            JSONObject timeRowHeader = new JSONObject();
            JSONObject timeRowTotal = new JSONObject();

            //Counters for enrollment, active, completed and time
            int totalMaleEnrollments = 0;
            int totalFemaleEnrollments = 0;
            int totalEnrollments = 0;
            int totalMaleActiveUsers = 0;
            int totalFemaleActiveUsers = 0;
            int totalActiveUsers = 0;
            int totalMaleCompleted = 0;
            int totalFemaleCompleted = 0;
            int totalCompleted = 0;
            long totalMaleTime = 0;
            long totalFemaleTime = 0;
            long totalTime = 0;

            String daysString = req.getParameter("days");
            String from = req.getParameter("from");
            String to = req.getParameter("to");


            int days=0;
            try {
                days = Integer.parseInt(daysString);
            }catch (Exception e){
                days = 0;
            }

            if(days < 1){
                days = DEFAULT_DAYS_TO_LOOK_BACK;
            }

            System.out.println("Days given: " + days);
            Long fromDate = getDateBeforeDays(days); //default
            if(from != null && !from.isEmpty()){
                fromDate = getDateFromString(from);
            }
            Long toDate = System.currentTimeMillis();
            if(to != null && !to.isEmpty()){
                toDate = getDateFromString(to);
            }
            System.out.println("From date: " + fromDate + " , to date: " + toDate);

            SummaryResult result = getSummaryResult(allUsers, fromDate, toDate, dbContext);

            List<String> enrolledUsersUsername = new LinkedList<>();
            for(User u:result.getEnrolled()){
                enrolledUsersUsername.add(u.getUsername());
            }
            //System.out.println("Populated enrolled Users");

            List<String> activeUsersUsername = new LinkedList<>();
            for(User u:result.getActive()){
                activeUsersUsername.add(u.getUsername());
            }
            //System.out.println("Populated active Users");

            List<String> completedUsersUsername = new LinkedList<>();
            for(User u:result.getCompleted()){
                completedUsersUsername.add(u.getUsername());
            }
            //System.out.println("Populated completd Users");

            //Duration map
            Map<String, Long> userDuration = result.getDurationMap();

            //Iterate over user university map
            //System.out.println("Iterating over university-><users> map..");
            Iterator<Map.Entry<String, List<User>>> userUniIterator = userToUniMap.entrySet().iterator();
            while(userUniIterator.hasNext()){
                Map.Entry<String, List<User>> uniUsers = userUniIterator.next();
                String university = uniUsers.getKey();
                List<User> users = uniUsers.getValue();

                JSONObject enrollment = new JSONObject();
                enrollment.put(MappingValues.COLUMN_TITLE, university);
                enrollment.put(MappingValues.COLUMN_NO_FEMALES, 0);
                enrollment.put(MappingValues.COLUMN_NO_MALES, 0);
                enrollment.put(MappingValues.COLUMN_TOTAL, 0);

                JSONObject active = new JSONObject();
                active.put(MappingValues.COLUMN_TITLE, university);
                active.put(MappingValues.COLUMN_NO_FEMALES, 0);
                active.put(MappingValues.COLUMN_NO_MALES, 0);
                active.put(MappingValues.COLUMN_TOTAL, 0);

                JSONObject completed = new JSONObject();
                completed.put(MappingValues.COLUMN_TITLE, university);
                completed.put(MappingValues.COLUMN_NO_FEMALES, 0);
                completed.put(MappingValues.COLUMN_NO_MALES, 0);
                completed.put(MappingValues.COLUMN_TOTAL, 0);

                JSONObject time = new JSONObject();
                time.put(MappingValues.COLUMN_TITLE, university);
                time.put(MappingValues.COLUMN_NO_FEMALES, 0);
                time.put(MappingValues.COLUMN_NO_MALES, 0);
                time.put(MappingValues.COLUMN_TOTAL, 0);

                //Calculate Enrollments (created)
                for(User user: users){

                    //Get User's Gender
                    String userGender = ucfManager.getUserField(user,
                            MappingValues.custom_fields_map.get("gender"),
                            dbContext).toLowerCase();

                    //Get enrollment data..
                    // ..(entry for this user and add it to enrolment entry for this uni)
                    if(enrolledUsersUsername.contains(user.getUsername())){
                        //Regardless of male/female/not specified, we add to total
                        totalEnrollments++;
                        enrollment.put(MappingValues.COLUMN_TOTAL,
                                (int)enrollment.get(MappingValues.COLUMN_TOTAL) + 1);

                        if(userGender.contains("female")){
                            //System.out.println("F");
                            totalFemaleEnrollments++;
                            enrollment.put(MappingValues.COLUMN_NO_FEMALES,
                                    (int)enrollment.get(MappingValues.COLUMN_NO_FEMALES) + 1);
                        }else
                        if(userGender.contains("male")){
                            totalMaleEnrollments++;
                            enrollment.put(MappingValues.COLUMN_NO_MALES,
                                    (int)enrollment.get(MappingValues.COLUMN_NO_MALES) + 1);
                        }


                    }
                    //System.out.println("enrollment total: " +
                    //        enrollment.get(MappingValues.COLUMN_TOTAL));

                    //Get active usage data
                    if (activeUsersUsername.contains(user.getUsername())) {
                        //Regardless of male/female/not specified, we add to total
                        totalActiveUsers++;
                        active.put(MappingValues.COLUMN_TOTAL,
                                (int)active.get(MappingValues.COLUMN_TOTAL) + 1);

                        if(userGender.contains("female")){
                            totalFemaleActiveUsers++;
                            active.put(MappingValues.COLUMN_NO_FEMALES,
                                    (int)active.get(MappingValues.COLUMN_NO_FEMALES) + 1);
                        }else
                        if(userGender.contains("male")){
                            totalMaleActiveUsers++;
                            active.put(MappingValues.COLUMN_NO_MALES,
                                    (int)active.get(MappingValues.COLUMN_NO_MALES) + 1);
                        }

                    }

                    //Get completed data
                    if (completedUsersUsername.contains(user.getUsername())) {
                        //Regardless of male/female/not specified, we add to total
                        totalCompleted++;
                        completed.put(MappingValues.COLUMN_TOTAL,
                                (int)completed.get(MappingValues.COLUMN_TOTAL) + 1);

                        if(userGender.contains("female")){
                            totalFemaleCompleted++;
                            completed.put(MappingValues.COLUMN_NO_FEMALES,
                                    (int)completed.get(MappingValues.COLUMN_NO_FEMALES) + 1);
                        }else
                        if(userGender.contains("male")){
                            totalMaleCompleted++;
                            completed.put(MappingValues.COLUMN_NO_MALES,
                                    (int)completed.get(MappingValues.COLUMN_NO_MALES) + 1);
                        }

                    }

                    //Get duration data
                    if(userDuration.containsKey(user.getUsername())){
                        long duration = userDuration.get(user.getUsername());
                        long durationSeconds = duration/1000;

                        if(userGender.contains("female")){
                            long currentFemaleTime = time.getLong(MappingValues.COLUMN_NO_FEMALES);
                            totalFemaleTime = totalFemaleTime + durationSeconds;
                            time.put(MappingValues.COLUMN_NO_FEMALES,
                                    currentFemaleTime + durationSeconds);
                        }else
                        if(userGender.contains("male")){
                            totalMaleTime = totalMaleTime+durationSeconds;
                            long currentMaleTime = time.getLong(MappingValues.COLUMN_NO_MALES);
                            time.put(MappingValues.COLUMN_NO_MALES,
                                    currentMaleTime + durationSeconds);

                        }

                        //System.out.println("male/female/total" + totalMaleTime + "/" +
                        //    totalFemaleTime + "/" + totalTime);
                        //Regardless of male/female/not specified, we add to total
                        long currentTotal = time.getLong(MappingValues.COLUMN_TOTAL);
                        totalTime = totalTime + durationSeconds;
                        time.put(MappingValues.COLUMN_TOTAL, currentTotal + durationSeconds);
                    }

                } //end of all users in this Uni loop.
                //System.out.println("");

                String uniMaleTimeReadable =
                        CourseUsageReportServlet.getDurationBreakdown(
                                time.getLong(MappingValues.COLUMN_NO_MALES) * 1000);
                String uniFemaleTimeReadable =
                        CourseUsageReportServlet.getDurationBreakdown(
                                time.getLong(MappingValues.COLUMN_NO_FEMALES) * 1000);
                String uniTimeReadable =
                        CourseUsageReportServlet.getDurationBreakdown(
                                time.getLong(MappingValues.COLUMN_TOTAL) * 1000);

                time.put(MappingValues.COLUMN_NO_MALES, uniMaleTimeReadable);
                time.put(MappingValues.COLUMN_NO_FEMALES, uniFemaleTimeReadable);
                time.put(MappingValues.COLUMN_TOTAL, uniTimeReadable);

                enrollmentRows.put(enrollment);
                activeRows.put(active);
                completedRows.put(completed);
                timeRows.put(time);

            }//end of uni-><Users> map loop
            //System.out.println("..iteration done.");

            /* handling it above, since we may need to add more genders/time
            totalEnrollments = totalMaleEnrollments + totalFemaleEnrollments;
            totalActiveUsers = totalMaleActiveUsers + totalFemaleActiveUsers;
            totalCompleted = totalMaleCompleted + totalFemaleCompleted;
            totalTime = totalMaleTime + totalFemaleTime;
            */

            //Calculating totals
            //System.out.println("Rendering values..");

            //Enrollment Totals:
            enrollmentRowHeader.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_NO_ENROLLMENT + "</b>");

            enrollmentRowTotal.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_TOTAL + "</b>");
            enrollmentRowTotal.put(MappingValues.COLUMN_NO_MALES, totalMaleEnrollments);
            enrollmentRowTotal.put(MappingValues.COLUMN_NO_FEMALES, totalFemaleEnrollments);
            enrollmentRowTotal.put(MappingValues.COLUMN_TOTAL, totalEnrollments);

            //Active usage totals
            activeRowHeader.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_NO_ACTIVE_USERS + "</b>");

            activeRowTotal.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_TOTAL + "</b>");
            activeRowTotal.put(MappingValues.COLUMN_NO_MALES, totalMaleActiveUsers);
            activeRowTotal.put(MappingValues.COLUMN_NO_FEMALES, totalFemaleActiveUsers);
            activeRowTotal.put(MappingValues.COLUMN_TOTAL, totalActiveUsers);

            //Completed usage totals
            completedRowHeader.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_NO_COMPLETED_USERS + "</b>");

            completedRowTotal.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_TOTAL + "</b>");
            completedRowTotal.put(MappingValues.COLUMN_NO_MALES, totalMaleCompleted);
            completedRowTotal.put(MappingValues.COLUMN_NO_FEMALES, totalFemaleCompleted);
            completedRowTotal.put(MappingValues.COLUMN_TOTAL, totalCompleted);

            //Duration totals
            timeRowHeader.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_TOTAL_TIME + "</b>");

            String totalMaleTimeReadable =
                    CourseUsageReportServlet.getDurationBreakdown(totalMaleTime * 1000);
            String totalFemaleTimeReadable =
                    CourseUsageReportServlet.getDurationBreakdown(totalFemaleTime * 1000);
            String totalTimeReadable =
                    CourseUsageReportServlet.getDurationBreakdown(totalTime * 1000);

            timeRowTotal.put(MappingValues.COLUMN_TITLE,
                    "<b>" + MappingValues.ROW_TOTAL + "</b>");
            timeRowTotal.put(MappingValues.COLUMN_NO_MALES, totalMaleTimeReadable);
            timeRowTotal.put(MappingValues.COLUMN_NO_FEMALES, totalFemaleTimeReadable);
            timeRowTotal.put(MappingValues.COLUMN_TOTAL, totalTimeReadable);


            reportJSONArray.put(enrollmentRowHeader);
            for(int i=0;i<enrollmentRows.length();i++){
                reportJSONArray.put(enrollmentRows.get(i));
            }
            reportJSONArray.put(enrollmentRowTotal);

            reportJSONArray.put(new JSONObject());

            reportJSONArray.put(activeRowHeader);
            for(int i=0;i<activeRows.length();i++){
                reportJSONArray.put(activeRows.get(i));
            }
            reportJSONArray.put(activeRowTotal);

            reportJSONArray.put(new JSONObject());

            reportJSONArray.put(completedRowHeader);
            for(int i=0;i<completedRows.length();i++){
                reportJSONArray.put(completedRows.get(i));
            }
            reportJSONArray.put(completedRowTotal);

            reportJSONArray.put(new JSONObject());

            reportJSONArray.put(timeRowHeader);
            for(int i=0;i<timeRows.length();i++){
                reportJSONArray.put(timeRows.get(i));
            }
            reportJSONArray.put(timeRowTotal);

            reportJSONArray.put(new JSONObject());

            jsonToReturn = reportJSONArray.toString();
            System.out.println("...rendering done.");

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
