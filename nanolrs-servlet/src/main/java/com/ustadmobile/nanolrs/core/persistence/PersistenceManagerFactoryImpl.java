package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.jdbc.persistence.PersistenceManagerJDBC;

import java.util.Properties;

/**
 * Created by Varuna on 4/13/2017.
 */
public class PersistenceManagerFactoryImpl {
    public PersistenceManager getPersistenceManager() {
        return new PersistenceManagerJDBC();
    }
}

