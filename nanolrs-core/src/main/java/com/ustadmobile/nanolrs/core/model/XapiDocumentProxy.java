package com.ustadmobile.nanolrs.core.model;

/**
 * Represents a Docuemnt with the Docuemnt Storage APIs (State, Activity Profile, Agent Profile)
 *
 * Created by mike on 10/2/16.
 */

public interface XapiDocumentProxy {

    String getId();

    void setId(String id);

    String getContentType();

    void setContentType(String contentType);

    String getContent();

    void setContent(String content);

}
