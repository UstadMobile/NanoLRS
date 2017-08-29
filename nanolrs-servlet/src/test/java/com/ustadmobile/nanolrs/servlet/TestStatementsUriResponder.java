package com.ustadmobile.nanolrs.servlet;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ustadmobile.nanolrs.servlet.TestUtilis;

/**
 * Created by Varuna on 4/11/2017.
 */
public class TestStatementsUriResponder extends Mockito {
    @Before
    public void beforeTests(){
        System.out.println("Hi, Before?");
    }



    @Test
    public void testServlet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        /* 1. Put a statement */
        JSONObject agentObj = new JSONObject();
        agentObj.put("mbox", "mailto:mike@ustadmobile.com");
        JSONObject stateObj = new JSONObject();
        stateObj.put("savekey", "saveval");
        byte[] jsonData = stateObj.toString().getBytes("UTF-8");

        String method = "put";
        String httpUsername = "username";
        String httpPassword = "password";
        String agentJson=agentObj.toString();
        String registration=null;
        String contentType="application/json";
        byte[] content=jsonData;


        when(request.getParameter("username")).thenReturn("me");
        when(request.getParameter("password")).thenReturn("secret");
        when(request.getMethod()).thenReturn(method);
        when(request.getParameter("agent")).thenReturn(agentJson);
        when(request.getHeader("content-type")).thenReturn(URLEncoder.encode(contentType, "UTF-8"));
        if(registration != null) {
            when(request.getParameter("registration")).thenReturn(registration);//UUID does not URL escaped
        }

        final ServletContext servletContext = Mockito.mock(ServletContext.class);

        when(request.getServletContext()).thenReturn(servletContext);
        ConnectionSource connectionSource = new JdbcPooledConnectionSource(TestUtilis.getJDBCUrl());
        servletContext.setAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE, connectionSource);
        when(request.getServletContext()).thenReturn(servletContext);
        when(request.getServletContext().getAttribute(
                NanoLrsContextListener.ATTR_CONNECTION_SOURCE)).thenReturn(connectionSource);


        InputStream stmtIn = getClass().getResourceAsStream(
                "/com/ustadmobile/nanolrs/core/xapi-statement-page-experienced.json");
        String stmtStr = LrsIoUtils.inputStreamToString(stmtIn);


        /*  contiue this
        //create POST body here
        ByteArrayInputStream statementByteArrayInputStream =
                new ByteArrayInputStream(stmtStr.getBytes());
        ServletInputStream servletInputStream = new ServletInputStream(){
            public int read() throws IOException {
                // fix this
                //return statementByteArrayInputStream.read();
                return 0;
            }
        };
        when(request.getInputStream()).thenReturn(servletInputStream);
        when(request.getReader()).thenReturn(
                new BufferedReader(new StringReader(stmtStr)));
        when(request.getContentType()).thenReturn("application/json");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");



        new StatementsServlet().doPut(request, response);
        System.out.println("PUT done.");
        verify(response).setContentType("application/json");
        verify(response).setStatus(200);

        */
        /* Return Check ID as json */
    }

}
