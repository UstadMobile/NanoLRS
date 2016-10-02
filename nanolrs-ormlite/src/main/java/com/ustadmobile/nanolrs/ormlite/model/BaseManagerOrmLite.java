package com.ustadmobile.nanolrs.ormlite.model;

import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

/**
 * Created by mike on 10/2/16.
 */

public abstract class BaseManagerOrmLite {

    protected PersistenceManagerORMLite persistenceManager;

    public BaseManagerOrmLite(PersistenceManagerORMLite persistenceManager) {
        this.persistenceManager = persistenceManager;
    }


}
