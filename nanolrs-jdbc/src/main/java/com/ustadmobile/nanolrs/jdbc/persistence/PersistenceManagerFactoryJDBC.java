package com.ustadmobile.nanolrs.jdbc.persistence;

import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

/**
 * Created by Varuna on 4/4/2017.
 */
public class PersistenceManagerFactoryJDBC {
    private PersistenceManagerJDBC persistenceManagerJDBC;

    public PersistenceManagerFactoryJDBC() {
    }

    public PersistenceManager getPersistenceManager() {
        if(this.persistenceManagerJDBC == null) {
            this.persistenceManagerJDBC = new PersistenceManagerJDBC();
        }

        return this.persistenceManagerJDBC;
    }
}
