package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.jdbc.persistence.PersistenceManagerJDBC;

/**
 * Created by Varuna on 4/4/2017.
 */
public class PersistenceManagerFactoryImpl {
    public PersistenceManager getPersistenceManager() {
        return new PersistenceManagerJDBC();
    }
}
