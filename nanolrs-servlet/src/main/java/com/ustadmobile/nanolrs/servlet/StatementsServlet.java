package com.ustadmobile.nanolrs.servlet;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Created by Varuna on 4/6/2017.
 */
public class StatementsServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //ToDo
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        //ToDo
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        String stmtId = request.getParameter("id");
        FileInputStream fin = null;

        try{
            byte[] requestContent = extractPostRequestBody(request).getBytes();
            JSONObject stmtObj = new JSONObject(new String(requestContent, "UTF-8"));
            ConnectionSource dbContext2 =
                    (ConnectionSource)request.getServletContext().getAttribute(
                            NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

            String storeId = XapiStatementsEndpoint.putStatement(stmtObj, dbContext2);
            response.setContentType("application/json");
            response.setStatus(200);
            /* TODO: Return ID as json */
            //response.

        }catch(IOException e){
            response.setContentType("text/plain");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

        }catch(JSONException j) {
            response.setContentType("text/plain");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "JSON Parse exception: " + j.getMessage());
        }finally {
            LrsIoUtils.closeQuietly(fin);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().log("service() called");
        //response.getWriter().write("Incrementing the count: count = " + count);
        if (request.getMethod() == "GET"){

        }else if(request.getMethod() == "PUT"){

        }else if(request.getMethod() == "POST"){

        }else{
            //Do nothing
        }
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

        if ("PUT".equalsIgnoreCase(request.getMethod())) {
            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }

        if ("POST".equalsIgnoreCase(request.getMethod()))
        {
            //return request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return "";
    }

}
