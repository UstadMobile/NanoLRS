package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerAndroid;

/**
 * Created by mike on 2/7/17.
 */

public class PersistenceManagerFactoryImpl {

    public PersistenceManager getPersistenceManager() {
        return new PersistenceManagerAndroid();
    }
}
