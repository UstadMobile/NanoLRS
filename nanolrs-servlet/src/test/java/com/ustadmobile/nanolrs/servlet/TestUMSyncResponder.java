package com.ustadmobile.nanolrs.servlet;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by varuna on 7/25/2017.
 */

public class TestUMSyncResponder  extends Mockito {

    @Before
    public void beforeTests(){
        System.out.println("Hi. Before?");

    }

    @Test
    public void testServlet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletContext servletContext = Mockito.mock(ServletContext.class);

        final ServletConfig servletConfig = mock(ServletConfig.class);

        String entitiesAsJSONString =
                "{    \"data\" : " +
                        "[" +
                        //New Entry
                        "{\"localSequence\":5,\"storedDate\":\"" + "0" + "\",\"dateCreated\":\"" + "0" + "\",\"masterSequence\":0,\"dateModifiedAtMaster\":0,\"pCls\":\"com.ustadmobile.nanolrs.core.model.User\",\"uuid\":\"" +  "test1" + "\",\"username\":\"test\"}" +

                        "]" +
                        ", \"info\" :" + "" +
                        " [" +
                        "{\"pCls\" : \"com.ustadmobile.nanolrs.core.model.User\",\"tableName\" : \"User\",\"count\" : " + "1" + ", \"pk\":\"uuid\"}" +
                        //"{\"pCls\" : \"com.ustadmobile.nanolrs.core.model.AnotherEntity\",\"tableName\" : \"AnotherEntity\",\"count\" : 0, \"pk\":\"uuid\"}" +
                        "]" +
                        "}"
                ;
        final byte[] jsonRequestBytes = entitiesAsJSONString.getBytes();


        String method = "POST";
        String httpUsername = "username";
        String httpPassword = "password";
        String activityId="http://www.ustadmobile.com/test/activity-state-id";
        String registration=null;
        String stateId="test_state_id";
        String contentType="application/json";

        Hashtable parameters = new Hashtable();
        parameters.clear();

        Hashtable headers = new Hashtable();
        headers.clear();
        headers.put("content-type",URLEncoder.encode(contentType, "UTF-8"));

        headers.put("useruuid","test1");
        headers.put("username","test");
        headers.put("password","secret");
        headers.put("isnewuser","true");
        headers.put("nodeuuid","client1");
        headers.put("hostname","client1");
        headers.put("hosturl","http://client1.com/sync/endpoint/");


        UMSyncServlet someServlet = new UMSyncServlet(){
            public ServletContext getServletContext() {
                return servletContext; // return the mock
            }
        };

        when(request.getServletContext()).thenReturn(servletContext);
        ConnectionSource connectionSource = new JdbcPooledConnectionSource(TestUtilis.getJDBCUrl());
        servletContext.setAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE, connectionSource);
        when(request.getServletContext()).thenReturn(servletContext);

        when(request.getServletContext().getAttribute(
                NanoLrsContextListener.ATTR_CONNECTION_SOURCE)).thenReturn(connectionSource);
        Mockito.doReturn(connectionSource).when(
                servletContext).getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE);

        when(servletConfig.getInitParameter("defaultPool")).thenReturn("testpool1");
        when(servletConfig.getServletContext()).thenReturn(servletContext);

        when(request.getInputStream()).thenReturn(
                new DelegatingServletInputStream(
                        new ByteArrayInputStream(jsonRequestBytes)));

        UMSyncServlet umSyncServlet = new UMSyncServlet();
        umSyncServlet.init(servletConfig);


        ServletOutputStream mockOutput = mock(ServletOutputStream.class);

        when(response.getOutputStream()).thenReturn(mockOutput);

        request = TestUtilis.mockMe(request, parameters, headers, method);

        //new UMSyncServlet().doPost(request, response);
        umSyncServlet.doPost(request, response);

        System.out.println("POST done.");
        verify(response).setContentType("application/json");
        verify(response).setStatus(200);


    }
}
