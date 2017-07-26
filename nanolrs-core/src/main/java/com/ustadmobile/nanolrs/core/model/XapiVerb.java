package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

/**
 * Created by mike on 9/13/16.
 */
public interface XapiVerb extends NanoLrsModelSyncable {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    @PrimaryKeyAnnotationClass(str="pk")
    String getVerbId();

    void setVerbId(String verbId);

    String getCanonicalData();

    void setCanonicalData(String canonicalData);

}
