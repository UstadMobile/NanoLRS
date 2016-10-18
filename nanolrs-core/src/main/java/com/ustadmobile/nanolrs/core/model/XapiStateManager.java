package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiStateManager {

    //XapiStateProxy findByActivityAndAgent(Object dbContext, XapiActivityProxy activity, XapiAgentProxy agent, String registrationUuid, String stateId);

    XapiStateProxy makeNew(Object dbContext);

    XapiStateProxy findByActivityAndAgent(Object dbContext, String activityId, String agentMbox, String agentAccountName, String agentAccountHomepage, String registrationUuid, String stateId);

    void persist(Object dbContext, XapiStateProxy data);

}
