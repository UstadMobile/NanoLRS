package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/12/16.
 */
public interface XapiActivity extends NanoLrsModel {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    String getActivityId();

    void setActivityId(String id);

    XapiAgent getAuthority();

    void setAuthority(XapiAgent authority);

    /**
     * @nanolrs.datatype LONG_STRING
     *
     * @return
     */
    String getCanonicalData();

    void setCanonicalData(String canonicalData);

}
