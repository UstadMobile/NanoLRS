package com.ustadmobile.nanolrs.ormlite.model;

import com.ustadmobile.nanolrs.core.model.XapiStateManager;
import com.ustadmobile.nanolrs.core.model.XapiStateProxy;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

/**
 * Created by mike on 10/2/16.
 */

public class XapiStateManagerOrmLite extends BaseManagerOrmLite implements XapiStateManager {

    public XapiStateManagerOrmLite(PersistenceManagerORMLite persistenceManager) {
        super(persistenceManager);
    }

    @Override
    public XapiStateProxy findByAgentJSON(String agentJSON, String registrationUuid) {
        return null;
    }
}
