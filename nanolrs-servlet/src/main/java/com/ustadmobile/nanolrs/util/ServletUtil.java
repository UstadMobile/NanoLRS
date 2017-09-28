package com.ustadmobile.nanolrs.util;

import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//jxl stuff:
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by varuna on 7/30/2017.
 */

public class ServletUtil {

    public static String REPORT_FILE_EXTENSION = ".csv";

    /**
     * Get username from basic auth in request
     * @param request
     * @return
     */
    public static String getUsernameFromBasicAuth(HttpServletRequest request){
        String[] credentials = getCredentialStringFromBasicAuth(request);
        return credentials[0];
    }

    /**
     * Get password from basic auth in request
     * @param request
     * @return
     */
    public static String getPasswordFromBasicAuth(HttpServletRequest request){
        String[] credentials = getCredentialStringFromBasicAuth(request);
        return credentials[1];
    }

    /**
     * Get username and password string from request's basic auth
     * @param request
     * @return
     */
    public static String[] getCredentialStringFromBasicAuth(HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = Base64Coder.decodeString(base64Credentials);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            return values;
        }
        return null;
    }

    /**
     * Get header value from request. This also takes into account old UM header names
     * @param request
     * @param headerName
     * @return
     */
    public static String getHeaderVal(HttpServletRequest request, String headerName){
        //Enabling support for old header names.
        String oldHeaderName = null;
        if(headerName.startsWith("X-UM-")){
            oldHeaderName = headerName.substring("X-UM-".length(), headerName.length());
        }
        if(request.getHeader(headerName) == null){
            String value = request.getHeader(oldHeaderName);
            //if(value!= null){
            //    System.out.println("OLD HEADER VALUE");
            //}
            return value;
        }
        return request.getHeader(headerName);
    }

    /**
     * Get specific parameter from request.
     * @param request
     * @param paramName
     * @return
     */
    public static String getParamVal(HttpServletRequest request, String paramName){
        return request.getParameter(paramName);
    }

    /**
     * Gets specific header name, value map from request.
     * @param request
     * @return
     */
    public static Map<String, String> getHeadersFromRequest(HttpServletRequest request){
        Map<String, String> requestHeaders = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames != null && headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            requestHeaders.put(headerName, getHeaderVal(request, headerName));
        }
        if(headerNames == null){
            return null;
        }
        return requestHeaders;
    }

    /**
     * Gets all headers from HttpServletRequest and returns it in a map
     * @param request
     * @return
     */
    public static Map<String, String> getParamsFromRequest(HttpServletRequest request){
        Map<String, String> requestParameters = new HashMap<>();
        Enumeration<String> parameterNames = request.getHeaderNames();
        while(parameterNames != null && parameterNames.hasMoreElements()){
            String paramName = parameterNames.nextElement();
            requestParameters.put(paramName, getHeaderVal(request, paramName));
        }
        if(parameterNames == null){
            return null;
        }
        return requestParameters;
    }

    /**
     * Converts an input stream to String
     * @param is    Inputstream
     * @param encoding  encoding (usually "UTF-8")
     * @return  the String
     * @throws IOException
     */
    public static String convertStreamToString(InputStream is, String encoding) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, encoding);
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    /**
     * encode username password string to basic auth
     * @param username
     * @param password
     * @return
     */
    public static String encodeBasicAuth(String username, String password) {
        return "Basic " + Base64Coder.encodeString(username +
                ':' + password);
    }

    /**
     * Returns userinfo for the user for the fields we need. TODO: loop over fields better
     * @param user
     * @param dbContext
     * @return
     * @throws SQLException
     */
    public static JSONObject getUserInfo(User user, Object dbContext) throws SQLException {
        //manager
        UserCustomFieldsManager userCustomFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

        JSONObject userInfo = new JSONObject();
        for(String field:MappingValues.USER_COLUMN_FIELDS){
            switch (field){
                case MappingValues.USER_COLUMN_GENDER:
                    String gender = userCustomFieldsManager.getUserField(
                            user, MappingValues.custom_fields_map.get(MappingValues.USER_COLUMN_GENDER), dbContext);
                    userInfo.put(MappingValues.USER_COLUMN_GENDER, gender);
                    break;
                case MappingValues.USER_COLUMN_TAZKIRA_ID:
                    String tazkira_id = userCustomFieldsManager.getUserField(
                            user, MappingValues.custom_fields_map.get(MappingValues.USER_COLUMN_TAZKIRA_ID), dbContext);
                    userInfo.put(MappingValues.USER_COLUMN_TAZKIRA_ID, tazkira_id);
                    break;
                case MappingValues.USER_COLUMN_FULLNAME:
                    String fullname = userCustomFieldsManager.getUserField(
                            user, MappingValues.custom_fields_map.get(MappingValues.USER_COLUMN_FULLNAME), dbContext);
                    userInfo.put(MappingValues.USER_COLUMN_FULLNAME, fullname);
                    break;
                case MappingValues.USER_COLUMN_USERNAME:
                    String usernameField = user.getUsername();
                    usernameField = "<a href=\"/syncendpoint/reports/userinfo/?username=" +
                            usernameField + "\">" + usernameField + "</a>";
                    userInfo.put(MappingValues.USER_COLUMN_USERNAME,usernameField);
                    break;
                case MappingValues.USER_COLUMN_UNIVERSITY:
                    //System.out.println("Getting university..");
                    String university = userCustomFieldsManager.getUserField(
                            user, MappingValues.custom_fields_map.get(MappingValues.USER_COLUMN_UNIVERSITY), dbContext);
                    if(university == null){
                        university = "";
                    }
                    //System.out.println("putting..");
                    userInfo.put(MappingValues.USER_COLUMN_UNIVERSITY, university);

                    if(MappingValues.uni_map.containsKey(university)){
                        userInfo.put("university", MappingValues.uni_map.get(university));
                    }else{
                        userInfo.put("university", "");
                    }
                    break;
                default:
                    break;
            }
        }
        return userInfo;
    }

    /**
     * TODO
     * @param user
     * @param dbContext
     * @return
     * @throws SQLException
     */
    public static JSONObject getUserColumnMap(User user, Object dbContext) throws SQLException {
        return null;
    }

    public static String excel_file_location;
    /**
     * Creates excel report from JSON Data (that represents a table shown on screen as report)
     * @param data
     * @return
     */
    public String makeExcelReport(JSONArray data, String reportName, String[] columnsInOrder){


        Date date = new Date();
        long unixTime = (long) date.getTime()/1000;
        excel_file_location = "report_" + unixTime + ".xls";

        //1. Create an Excel file
        WritableWorkbook myFirstWbook = null;
        try {

            myFirstWbook = Workbook.createWorkbook(new File(excel_file_location));

            // create an Excel sheet
            WritableSheet excelSheet = myFirstWbook.createSheet(reportName, 0);

            Label[] headers = new Label[columnsInOrder.length];
            /*
            for(int j=0; j<columnsInOrder.length; j++){{
                Label thisLabel = new Label(j, 0, columnsInOrder[j]);
                headers[j] = thisLabel;
            }

            for(int i=0;i<data.length();i++){
                JSONObject entry = data.getJSONObject(i);
                for(Label header:headers){

                }
                for(String col:columnsInOrder){
                    entry.get(col);
                }

            }
            */

            /*
            // add something into the Excel sheet
            Label label = new Label(0, 0, "Test Count");
            excelSheet.addCell(label);

            Number number = new Number(0, 1, 1);
            excelSheet.addCell(number);

            label = new Label(1, 0, "Result");
            excelSheet.addCell(label);

            label = new Label(1, 1, "Passed");
            excelSheet.addCell(label);

            number = new Number(0, 2, 2);
            excelSheet.addCell(number);

            label = new Label(1, 2, "Passed 2");
            excelSheet.addCell(label);
            */

            myFirstWbook.write();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (myFirstWbook != null) {
                try {
                    myFirstWbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            }


        }

        return excel_file_location;
    }

    /**
     * Convert String to HashMap.
     * @param value
     * @return
     */
    public static LinkedHashMap<String, String> getLinkedMapFromString(String value){
        value = value.substring(1, value.length()-1);           //remove curly brackets {}
        String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
        LinkedHashMap<String,String> map = new LinkedHashMap<>();

        for(String pair : keyValuePairs)                        //iterate over the pairs
        {
            String[] entry = pair.split("=");                   //split the pairs to get key and value
            if(entry.length > 1 && !entry[0].trim().isEmpty() ){
                map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap and trim whitespaces
            }
        }
        return map;
    }

    /**
     * Modified from CDL to include outputting comma separated string in order that we give it
     * @param ja
     * @return
     * @throws JSONException
     */
    public static String jsonArrayToStringInOrder(JSONArray ja,
                      LinkedHashMap<String, String> table_headers_html_map) throws JSONException {

        JSONArray orderedNames = new JSONArray();
        JSONArray orderedLabels = new JSONArray();
        Iterator<Map.Entry<String, String>> header_iterator = table_headers_html_map.entrySet().iterator();
        while(header_iterator.hasNext()){
            Map.Entry<String, String> e = header_iterator.next();
            String name = e.getKey();
            orderedNames.put(name);

            String label = e.getValue();
            orderedLabels.put(label);
        }

        JSONObject jo = ja.optJSONObject(0);
        if (jo != null) {
            JSONArray names = jo.names(); //not in order so we do it our way..
            /*
            if (names != null) {
                return CDL.rowToString(names) + CDL.toString(names, ja);
            }
            */
            if(orderedNames != null){
                return CDL.rowToString(orderedLabels) + CDL.toString(orderedNames, ja);
            }
        }
        return null;
    }

    /**
     * Converts json to CSV
     * @param data the JSONArray
     */
    public static File jsonToReportFile(JSONArray data,
                                        LinkedHashMap<String, String> table_headers_html_map){

        Date date = new Date();
        long unixTime = date.getTime()/1000;
        excel_file_location = "report_" + unixTime + REPORT_FILE_EXTENSION;

        try {
            File dir = new File("reports");
            dir.mkdir();
            File file=new File(dir, excel_file_location);
            System.out.println("File path : " + file.getAbsolutePath());

            String csv = jsonArrayToStringInOrder(data, table_headers_html_map);
            FileUtils.writeStringToFile(file, csv); //Deprecated, but allowing. TODO: check this

            return file;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check users variable against choosen filters
     * @param user_variable
     * @param filters
     * @return
     */
    public static boolean shouldIShowThisUserWithFilter(String user_variable, ArrayList filters ){
        /* University Filter */

        boolean iWantToBreakFree = false;
        if (filters.isEmpty()) {
            //Let it go.. Let it go..
            //System.out.println("No Uni filter selected. Showing all users..");
        }else if(filters.contains("ALL")){
            //Let it go, Let it go..
            //System.out.println("All Uni filter selected. Showing all users..");
        }else if(filters.contains("Other") ||
                filters.contains("I don't know")){
            if(user_variable.contains("Other") || user_variable.contains("I don't know") ||
                    user_variable == null || user_variable.isEmpty() ||
                    user_variable.trim().isEmpty()){
                //Let it go..
                //System.out.println("Selected Other/I don't know. " +
                //       "User's uni is also that. Allowing..");
            }else{
                iWantToBreakFree = true;
            }
        }else if(!filters.contains(user_variable)){
            iWantToBreakFree = true;
        }

        return iWantToBreakFree;
    }

    /**
     * To escape string
     * @param string
     * @return
     */
    public static String stringToHTMLString(String string) {
        StringBuffer sb = new StringBuffer(string.length());
        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = string.length();
        char c;

        for (int i = 0; i < len; i++)
        {
            c = string.charAt(i);
            if (c == ' ') {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                }
                else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            }
            else {
                lastWasBlankChar = false;
                //
                // HTML Special Chars
                if (c == '"')
                    sb.append("&quot;");
                else if (c == '&')
                    sb.append("&amp;");
                else if (c == '<')
                    sb.append("&lt;");
                else if (c == '>')
                    sb.append("&gt;");
                else if (c == '\n')
                    // Handle Newline
                    sb.append("&lt;br/&gt;");
                else {
                    int ci = 0xffff & c;
                    if (ci < 160 )
                        // nothing special only 7 Bit
                        sb.append(c);
                    else {
                        // Not 7 Bit use the unicode system
                        sb.append("&#");
                        sb.append(new Integer(ci).toString());
                        sb.append(';');
                    }
                }
            }
        }
        return sb.toString();
    }

}
