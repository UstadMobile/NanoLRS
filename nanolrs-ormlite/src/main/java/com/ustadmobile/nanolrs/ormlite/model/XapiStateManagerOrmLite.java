package com.ustadmobile.nanolrs.ormlite.model;

import com.ustadmobile.nanolrs.core.endpoints.XapiActivityEndpoint;
import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
import com.ustadmobile.nanolrs.core.model.XapiAgentProxy;
import com.ustadmobile.nanolrs.core.model.XapiStateManager;
import com.ustadmobile.nanolrs.core.model.XapiStateProxy;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import org.json.JSONObject;

/**
 * Created by mike on 10/2/16.
 */

public class XapiStateManagerOrmLite extends BaseManagerOrmLite implements XapiStateManager {

    public XapiStateManagerOrmLite(PersistenceManagerORMLite persistenceManager) {
        super(persistenceManager);
    }

    @Override
    public XapiStateProxy findByActivityAndAgent(Object dbContext, String activityId, String agentJSON, String registrationUuid, String stateId) {
        JSONObject activityJSON = new JSONObject();
        activityJSON.put("id", activityId);
        XapiActivityProxy activity = XapiActivityEndpoint.createOrUpdateById(dbContext, activityJSON);



        return null;
    }
}
