package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.jdbc.persistence.PersistenceManagerJDBC;

/**
 * Created by mike on 2/7/17.
 */

public class PersistenceManagerFactoryImpl {

    public PersistenceManager getPersistenceManager() {
        return new PersistenceManagerJDBC();
    }

}
