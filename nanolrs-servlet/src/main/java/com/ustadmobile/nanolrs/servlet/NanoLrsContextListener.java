package com.ustadmobile.nanolrs.servlet;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Varuna on 4/17/2017.
 */
public class NanoLrsContextListener implements ServletContextListener {

    public static final String ATTR_CONNECTION_SOURCE = "connectionSource";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        File webInfDir = new File(context.getRealPath("/WEB-INF"));
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(webInfDir, "buildconfig.default.properties")));
            File localProperties = new File(webInfDir, "buildconfig.local.properties");
            if(localProperties.exists()) {
                props.load(new FileInputStream(localProperties));
            }

            String jdbcUrl = props.getProperty("JDBCURL");
            ConnectionSource connectionSource = new JdbcPooledConnectionSource(jdbcUrl);
            context.setAttribute(ATTR_CONNECTION_SOURCE, connectionSource);
        }catch(Exception e) {
            System.err.println("Exception!");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
