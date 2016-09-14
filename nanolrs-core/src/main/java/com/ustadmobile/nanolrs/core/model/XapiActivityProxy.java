package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/12/16.
 */
public interface XapiActivityProxy {

    String getActivityId();

    void setActivityId(String id);

    XapiAgentProxy getAuthority();

    void setAuthority(XapiAgentProxy authority);

    String getCanonicalData();

    void setCanonicalData(String canonicalData);

}
