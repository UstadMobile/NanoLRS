package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/13/16.
 */
public interface XapiVerb extends NanoLrsModel {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    String getVerbId();

    void setVerbId(String verbId);

    String getCanonicalData();

    void setCanonicalData(String canonicalData);

}
