package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiStateManager {

    XapiStateProxy findByActivityAndAgent(Object dbContext, String activityId, String agentJSON, String registrationUuid, String stateId);

}
