package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.jdbc.persistence.PersistenceManagerJDBC;

import java.util.Properties;

/**
 * Created by Varuna on 4/13/2017.
 *
 * This is this platform's PersistenceManagerFactoryImpl (ie nanolrs-servlet's).
 * Its getPersistenceManager() will return a new JDBC Persistence Manager (in nanolrs-jdbc)
 */
public class PersistenceManagerFactoryImpl {
    public PersistenceManager getPersistenceManager() {
        return new PersistenceManagerJDBC();
    }
}

