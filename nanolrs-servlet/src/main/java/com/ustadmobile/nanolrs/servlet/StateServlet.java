package com.ustadmobile.nanolrs.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.endpoints.XapiStateEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;

import java.util.Scanner;

/**
 * Created by Varuna on 4/6/2017.
 */
public class StateServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        getServletContext().log("init() called");
    }

    /* Gets Parameter */
    private String getFirstParamVal(HttpServletRequest request, String paramName) {
        return request.getParameter(paramName);
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().log("service() called");

    }

    /**
     * doGet
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String activityId = getFirstParamVal(request, "activityId");
        String agentJson = getFirstParamVal(request, "agent");
        String registration = getFirstParamVal(request, "registration");
        String stateId = getFirstParamVal(request, "stateId");
        ConnectionSource dbContext = (ConnectionSource)request.getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);
        XapiState stateProxy = XapiStateEndpoint.getState(dbContext, activityId, agentJson, registration, stateId);
        if (stateProxy == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            response.setStatus(404);
        }else {
            byte[] stateData = stateProxy.getContent();
            int length = stateData.length;

            response.setContentType(stateProxy.getContentType());
            response.setContentLength(stateData.length);
            ServletOutputStream sos = response.getOutputStream();
            sos.write(stateData);
            sos.close();
            response.setStatus(200);
        }

    }

    /**
     * doPOST
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{
        /*
        UMSyncResult result = null;
        response.setStatus(result.getStatus());
        InputStream dataIn = result.getResponseData();
        OutputStream clientOut = response.getOutputStream();
        byte[] buf = new byte[1024];
        int bytesRead;
        while((bytesRead = dataIn.read(buf, 0, buf.length)) != -1) {
            clientOut.write(buf, 0, bytesRead);
        }
        */

        try {
            putPost(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{

        String activityId = getFirstParamVal(request, "activityId");
        String agentJson = getFirstParamVal(request, "agent");
        String registration = getFirstParamVal(request, "registration");
        String stateId = getFirstParamVal(request, "stateId");
        ConnectionSource dbContext = (ConnectionSource)request.getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);
        XapiStateEndpoint.delete(dbContext, activityId, agentJson, registration, stateId);
        response.setStatus(200);
        response.setContentType("application/json");
    }

    /**
     * Common method for PUT and POST
     * @param request
     * @param response
     * @throws SQLException
     * @throws IOException
     */
    protected void putPost(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String activityId = getFirstParamVal(request, "activityId");
        String agentJson = getFirstParamVal(request, "agent");
        String registration = getFirstParamVal(request, "registration");
        String stateId = getFirstParamVal(request, "stateId");

        if(activityId == null || agentJson == null || stateId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        byte[] stateData = extractPostRequestBody(request).getBytes();
        ConnectionSource dbContext = (ConnectionSource)request.getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        XapiStateEndpoint.createOrUpdateState(dbContext, request.getMethod().toString(),
                request.getHeader("content-type"),
                activityId,agentJson, registration, stateId, stateData);

        response.setContentType("application/json");
        response.setStatus(200);
    }

    @Override
    public void destroy() {
        getServletContext().log("destroy() called");
    }

    /* Do this */
    static String extractPostRequestBody(HttpServletRequest request) throws IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        return "";
    }
}
