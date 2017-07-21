package com.ustadmobile.nanolrs.test.core;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.buildconfig.TestConstantsServlet;

import java.sql.SQLException;

/**
 * Created by mike on 2/7/17.
 *
 * This class is the platform test util class (that is present in other platforms as well. Since this
 *  module is nanolrs-sevlet, here we have the connection source made from JDBC pool. We create that
 *  connectionSource from the JDBC URL thats gets generated in TestConstants (that is auto generated
 *  from properties file in another pre process. We have defaulted that to JDBC mem url)
 *
 *  This class will get called when TestSuite is run.
 *  Every test in testSuite has a NanoLrsPlatformTestUtil.getContext() method call. This is
 *  required before we begin testing every tests. In this case the test will look in where the test
 *  is running (in this case, nanolrs-servlet. Specifically this Servlet's test util will get
 *  called when NanoLrsTestSuiteServlet test is run/debugged.
 *
 *  The purpose of NanoLrsPlatformTestUtil is to set the connectionSource from the platforms
 *  connection pool (or create one).
 */

public class NanoLrsPlatformTestUtil {

    public static ConnectionSource connectionSource;

    public static ConnectionSource endpointConnectionSource;

    public static Object getContext() {
        if(connectionSource == null) {

            try {
                connectionSource = new JdbcConnectionSource(TestConstantsServlet.TEST_JDBC_URL);
            }catch(SQLException se){
                se.printStackTrace();
                System.out.println( "DB Sync Sql Exception! " + se );
            }

        }

        return connectionSource;
    }

    public static Object getSyncEndpointContext() {
        if(endpointConnectionSource == null) {
            try {
                endpointConnectionSource = new JdbcConnectionSource(TestConstantsServlet.TEST_JDBC_URL_ENDPOINT);
            }catch(SQLException e) {
                e.printStackTrace();
            }
        }
        return endpointConnectionSource;
    }

}
