package com.ustadmobile.nanolrs.servlet;


import java.io.*;
import java.net.URLEncoder;
import java.util.Hashtable;

import javax.servlet.http.*;
import javax.servlet.ServletOutputStream;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.ustadmobile.nanolrs.servlet.TestUtilis;

/**
 * Created by Varuna on 4/11/2017.
 */
public class TestStateUriResponder extends Mockito {

    public static final int TESTPORT = 8071;
    protected String endpoint = "http://localhost:" + TESTPORT + "/xapi/";


    @Before
    public void beforeTests(){
        System.out.println("Hi. Before?");

    }

    @Test
    public void testServlet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        /*
        1. Put a state
        2. Get the state by statid & actor
        3. get the state by different actor (shouldn't come back: 404)
        4. Delete the state
        5. Check (404)
         */

        /* 1. Put a state */
        JSONObject agentObj = new JSONObject();
        agentObj.put("mbox", "mailto:mike@ustadmobile.com");
        JSONObject stateObj = new JSONObject();
        stateObj.put("savekey", "saveval");
        byte[] jsonData = stateObj.toString().getBytes("UTF-8");

        String method = "put";
        String httpUsername = "username";
        String httpPassword = "password";
        String activityId="http://www.ustadmobile.com/test/activity-state-id";
        String agentJson=agentObj.toString();
        String registration=null;
        String stateId="test_state_id";
        String contentType="application/json";
        byte[] content=jsonData;

        Hashtable parameters = new Hashtable();
        parameters.clear();
        parameters.put("username","username");
        parameters.put("password","password");
        parameters.put("activityId",URLEncoder.encode(activityId, "UTF-8"));
        parameters.put("agent",agentJson);
        if(registration != null) {
            parameters.put("registration", registration);
        }
        parameters.put("stateId", URLEncoder.encode(stateId, "UTF-8"));


        Hashtable headers = new Hashtable();
        headers.clear();
        headers.put("content-type",URLEncoder.encode(contentType, "UTF-8"));

        request = TestUtilis.mockMe(request, parameters, headers, method);

        new StateServlet().doPost(request, response);

        System.out.println("PUT done.");
        verify(response).setContentType("application/json");
        verify(response).setStatus(200);

        /* 2. Get the state by statid & actor */
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        method = "get";

        parameters.clear();
        parameters.put("username","username");
        parameters.put("password","password");
        parameters.put("activityId",URLEncoder.encode(activityId, "UTF-8"));
        parameters.put("agent",agentJson);
        if(registration != null) {
            parameters.put("registration", registration);
        }
        parameters.put("stateId", URLEncoder.encode(stateId, "UTF-8"));

        headers.clear();
        headers.put("content-type",URLEncoder.encode(contentType, "UTF-8"));

        request = TestUtilis.mockMe(request, parameters, headers, method);

        ServletOutputStream mockOutput = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(mockOutput);

        new StateServlet().doGet(request, response);
        System.out.println("GET done.");
        verify(response).setStatus(200);

        /* 3. get the state by different actor (shouldn't come back: 404) */
        JSONObject agentObj_wrong = new JSONObject();
        agentObj_wrong.put("mbox", "mailto:varuna@ustadmobile.com");
        String agentJson_wrong=agentObj_wrong.toString();
        method = "get";

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        parameters.clear();
        parameters.put("username","username");
        parameters.put("password","password");
        parameters.put("activityId",URLEncoder.encode(activityId, "UTF-8"));
        parameters.put("agent",agentJson_wrong);
        if(registration != null) {
            parameters.put("registration", registration);
        }
        parameters.put("stateId", URLEncoder.encode(stateId, "UTF-8"));

        headers.clear();
        headers.put("content-type",URLEncoder.encode(contentType, "UTF-8"));

        request = TestUtilis.mockMe(request, parameters, headers, method);

        new StateServlet().doGet(request, response);
        System.out.println("GET (404) done.");
        verify(response).setStatus(404);

        /* 4. Delete the state */
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        method="delete";

        parameters.clear();
        parameters.put("username","username");
        parameters.put("password","password");
        parameters.put("activityId",URLEncoder.encode(activityId, "UTF-8"));
        parameters.put("agent",agentJson);
        if(registration != null) {
            parameters.put("registration", registration);
        }
        parameters.put("stateId", URLEncoder.encode(stateId, "UTF-8"));


        headers.clear();
        headers.put("content-type",URLEncoder.encode(contentType, "UTF-8"));

        request = TestUtilis.mockMe(request, parameters, headers, method);

        mockOutput = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(mockOutput);

        new StateServlet().doDelete(request, response);
        System.out.println("Delete done.");
        verify(response).setStatus(200);

        /* 5. Check (404) */
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        method="get";

        parameters.clear();
        parameters.put("username","username");
        parameters.put("password","password");
        parameters.put("activityId",URLEncoder.encode(activityId, "UTF-8"));
        parameters.put("agent",agentJson);
        if(registration != null) {
            parameters.put("registration", registration);
        }
        parameters.put("stateId", URLEncoder.encode(stateId, "UTF-8"));

        headers.clear();
        headers.put("content-type",URLEncoder.encode(contentType, "UTF-8"));

        request = TestUtilis.mockMe(request, parameters, headers, method);

        new StateServlet().doGet(request, response);
        System.out.println("Get post delete done.");
        verify(response).setStatus(404);

    }

    /**
     * Returns the URL for talking to the LRS about a given statement
     *
     * @param xapiBaseURL
     * @param activityId
     * @param agentJson
     * @param registration
     * @param stateId
     * @return
     */
    private String makeStateURL(String xapiBaseURL, String activityId, String agentJson, String registration, String stateId) {
        String destURL = xapiBaseURL;

        if(!destURL.endsWith("/")) {
            destURL += "/";
        }
        try {
            destURL += "activities/state";
            destURL += "?activityId=" + URLEncoder.encode(activityId, "UTF-8");
            destURL += "&agent=" + URLEncoder.encode(agentJson, "UTF-8");
            if(registration != null) {
                destURL += "&registration=" + registration;//UUID does not URL escaped
            }
            destURL += "&stateId=" + URLEncoder.encode(stateId, "UTF-8");
            return destURL;
        }catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);//NO UTF-8 - Not going to happen!
        }
    }

}
