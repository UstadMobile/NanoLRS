package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiStateManager {

    XapiStateProxy findByAgentJSON(String agentJSON, String registrationUuid);

}
