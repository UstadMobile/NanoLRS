package com.ustadmobile.nanolrs.test.core;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.buildconfig.TestConstantsJDBC;

import java.sql.SQLException;

/**
 * Created by mike on 2/7/17.
 */

public class NanoLrsPlatformTestUtil {

    public static ConnectionSource connectionSource;

    public static Object getContext() {
        if(connectionSource == null) {

            try {
                connectionSource = new JdbcConnectionSource(TestConstantsJDBC.TEST_JDBC_URL);
            }catch(SQLException se){
                se.printStackTrace();
                System.out.println( "DB Sync Sql Exception! " + se );
            }

        }

        return connectionSource;
    }

}
