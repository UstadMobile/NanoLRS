package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/12/16.
 */
public interface XapiActivity {

    String getActivityId();

    void setActivityId(String id);

    XapiAgent getAuthority();

    void setAuthority(XapiAgent authority);

    String getCanonicalData();

    void setCanonicalData(String canonicalData);

}
