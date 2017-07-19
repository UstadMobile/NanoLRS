package com.ustadmobile.nanolrs.core.model;

/**
 * Represents a Docuemnt with the Docuemnt Storage APIs (State, Activity Profile, Agent Profile)
 *
 * Created by mike on 10/2/16.
 */

public interface XapiDocument extends NanoLrsModelSyncable{

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    String getUuid();

    void setUuid(String id);

    String getContentType();

    void setContentType(String contentType);

    /**
     * @nanolrs.datatype LONG_STRING
     *
     * @return
     */
    String getContent();

    void setContent(String content);

}
