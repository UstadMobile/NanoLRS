package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiState extends NanoLrsModelSyncable{

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    @PrimaryKeyAnnotationClass(str="pk")
    String getUuid();

    void setUuid(String uuid);

    XapiActivity getActivity();

    void setActivity(XapiActivity activity);

    XapiAgent getAgent();

    void setAgent(XapiAgent agent);

    String getRegistration();

    void setRegistration(String registration);

    String getStateId();

    void setStateId(String stateId);

    long getDateStored();

    void setDateStored(long dateStored);

    XapiDocument getDocument();

    void setDocument(XapiDocument document);

    /**
     * @nanolrs.datatype BYTE_ARRAY
     * @return
     */
    byte[] getContent();

    void setContent(byte[] stateContent);

    String getContentType();

    void setContentType(String contentType);

}
