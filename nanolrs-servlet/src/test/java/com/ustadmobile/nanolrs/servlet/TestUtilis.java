package com.ustadmobile.nanolrs.servlet;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;


import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ustadmobile.nanolrs.buildconfig.TestConstantsServlet;
import com.ustadmobile.nanolrs.util.ServletUtil;

/**
 * Created by Varuna on 4/18/2017.
 */
public class TestUtilis {

    /**
     * Get header with oldHeader check.
     * @param headers
     * @param headerName
     * @return
     */
    public static String getHeader(Hashtable headers, String headerName){
        //Enabling support for old header names.
        String oldHeaderName = null;
        if(headerName.startsWith("X-UM-")){
            oldHeaderName = headerName.substring("X-UM-".length(), headerName.length());
        }
        if(headers.get(headerName) == null){
            String value = headers.get(oldHeaderName).toString();
            if(value!= null){
                System.out.println("OLD HEADER VALUE");
            }
            return value;
        }
        return headers.get(headerName).toString();
    }
    public static HttpServletRequest mockMe(HttpServletRequest request, Hashtable parameters,
                                     Hashtable headers, String method ) throws SQLException {
        String param;
        String value;
        String header;
        Enumeration parameternames = parameters.keys();
        Enumeration headernames = headers.keys();

        //request = mock(HttpServletRequest.class);

        while(parameternames.hasMoreElements()){
            param = (String) parameternames.nextElement();
            value = (String)parameters.get(param);
            when(request.getParameter(param)).thenReturn(value);
        }
        when(request.getMethod()).thenReturn(method);
        while(headernames.hasMoreElements()){
            header = (String) headernames.nextElement();
            //value = (String)headers.get(header);
            value = getHeader(headers,header);
            when(ServletUtil.getHeaderVal(request, header)).thenReturn(value);
        }

        /* setting the connection source to servlet context's attribute */
        ConnectionSource connectionSource = new JdbcPooledConnectionSource(TestUtilis.getJDBCUrl());
        final ServletContext servletContext = mock(ServletContext.class);
        servletContext.setAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE, connectionSource);
        when(request.getServletContext()).thenReturn(servletContext);
        when(request.getServletContext().getAttribute(NanoLrsContextListener.ATTR_CONNECTION_SOURCE)).thenReturn(connectionSource);

        return request;
    }

    public static String getJDBCUrl(){
        return TestConstantsServlet.TEST_JDBC_URL;

    }
}
