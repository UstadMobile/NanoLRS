package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

/**
 * Created by mike on 9/12/16.
 */
public interface XapiActivity extends NanoLrsModelSyncable {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    @PrimaryKeyAnnotationClass(str="pk")
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
