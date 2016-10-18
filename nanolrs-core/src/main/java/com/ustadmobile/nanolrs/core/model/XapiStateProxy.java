package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiStateProxy {

    //Generic UUID
    String getId();

    void setId(String id);

    XapiActivityProxy getActivity();

    void setActivity(XapiActivityProxy activity);

    XapiAgentProxy getAgent();

    void setAgent(XapiAgentProxy agent);

    String getRegistration();

    void setRegistration(String registration);

    String getStateId();

    void setStateId(String stateId);

    long getDateStored();

    void setDateStored(long dateStored);

    XapiDocumentProxy getDocument();

    void setDocument(XapiDocumentProxy document);

    byte[] getContent();

    void setContent(byte[] stateContent);

    String getContentType();

    void setContentType(String contentType);

}
