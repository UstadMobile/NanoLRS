package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiState;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiStateManager extends NanoLrsManagerSyncable {

    //XapiStateProxy findByActivityAndAgent(Object dbContext, XapiActivityProxy activity, XapiAgentProxy agent, String registrationUuid, String stateId);

    XapiState makeNew(Object dbContext);

    XapiState findByActivityAndAgent(Object dbContext, String activityId, String agentMbox, String agentAccountName, String agentAccountHomepage, String registrationUuid, String stateId);

    void persist(Object dbContext, XapiState data);

    boolean delete(Object dbContext, XapiState data);

}
