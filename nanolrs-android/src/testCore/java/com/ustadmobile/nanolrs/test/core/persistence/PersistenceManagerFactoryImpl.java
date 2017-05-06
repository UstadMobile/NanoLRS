package com.ustadmobile.nanolrs.test.core.persistence;

import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.jdbc.persistence.PersistenceManagerJDBC;

/**
 * Created by mike on 5/3/17.
 */

public class PersistenceManagerFactoryImpl {

    public PersistenceManager getPersistenceManager() {
        return new PersistenceManagerJDBC();
    }
}
