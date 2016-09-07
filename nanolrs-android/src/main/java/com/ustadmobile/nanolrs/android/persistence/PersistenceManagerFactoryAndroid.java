package com.ustadmobile.nanolrs.android.persistence;

import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManagerFactory;

/**
 * Created by mike on 9/6/16.
 */
public class PersistenceManagerFactoryAndroid implements PersistenceManagerFactory{

    private PersistenceManagerAndroid persistenceManagerAndroid;

    @Override
    public PersistenceManager getPersistenceManager() {
        if(persistenceManagerAndroid == null) {
            persistenceManagerAndroid = new PersistenceManagerAndroid();
        }

        return persistenceManagerAndroid;
    }
}
