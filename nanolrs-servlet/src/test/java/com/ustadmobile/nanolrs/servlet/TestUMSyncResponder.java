package com.ustadmobile.nanolrs.servlet;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;

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

        String method = "POST";
        String httpUsername = "username";
        String httpPassword = "password";
        String activityId="http://www.ustadmobile.com/test/activity-state-id";
        String registration=null;
        String stateId="test_state_id";
        String contentType="application/json";

        String entitiesAsJSONString =
                "{    \"data\" : " +
                        "[" +
                        //New Entry
                        "{\"localSequence\":5,\"storedDate\":\"" + "0" + "\",\"dateCreated\":\"" + "0" + "\",\"masterSequence\":0,\"dateModifiedAtMaster\":0,\"pCls\":\"com.ustadmobile.nanolrs.core.model.User\",\"uuid\":\"" +  "test1" + "\",\"username\":\"test\",\"password\":\"secret\"}" +

                        "]" +
                        ", \"info\" :" + "" +
                        " [" +
                        "{\"pCls\" : \"com.ustadmobile.nanolrs.core.model.User\",\"tableName\" : \"User\",\"count\" : " + "1" + ", \"pk\":\"username\"}" +
                        //"{\"pCls\" : \"com.ustadmobile.nanolrs.core.model.AnotherEntity\",\"tableName\" : \"AnotherEntity\",\"count\" : 0, \"pk\":\"uuid\"}" +
                        "]" +
                        "}"
                ;
        final byte[] jsonRequestBytes = entitiesAsJSONString.getBytes();


        Hashtable parameters = new Hashtable();
        parameters.clear();

        Hashtable headers = new Hashtable();
        headers.clear();

        headers.put(UMSyncEndpoint.REQUEST_CONTENT_TYPE,URLEncoder.encode(contentType, UMSyncEndpoint.UTF_ENCODING));

        headers.put(UMSyncEndpoint.HEADER_USER_UUID,"test1");
        headers.put(UMSyncEndpoint.HEADER_USER_USERNAME,"test");
        headers.put(UMSyncEndpoint.HEADER_USER_PASSWORD,"secret");
        headers.put(UMSyncEndpoint.HEADER_USER_IS_NEW,"false"); //Not new anymore..
        headers.put(UMSyncEndpoint.HEADER_NODE_UUID,"client1");
        headers.put(UMSyncEndpoint.HEADER_NODE_NAME,"client1");
        headers.put(UMSyncEndpoint.HEADER_NODE_ROLE, "client");
        headers.put(UMSyncEndpoint.HEADER_NODE_HOST, "client1");
        headers.put(UMSyncEndpoint.HEADER_NODE_URL,"http://client1.com/sync/endpoint/");


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

        ConnectionSource dbContext = new JdbcPooledConnectionSource(TestUtilis.getJDBCUrl());

        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        Node servletNode = nodeManager.createThisDeviceNode("test","test","/test/",true, false, dbContext);

        ServletOutputStream mockOutput = mock(ServletOutputStream.class);

        when(response.getOutputStream()).thenReturn(mockOutput);

        request = TestUtilis.mockMe(request, parameters, headers, method);

        //new UMSyncServlet().doPost(request, response);
        umSyncServlet.doPost(request, response);

        System.out.println("POST done.");
        verify(response).setContentType(UMSyncEndpoint.JSON_MIMETYPE);
        verify(response).setStatus(200);


    }
}
