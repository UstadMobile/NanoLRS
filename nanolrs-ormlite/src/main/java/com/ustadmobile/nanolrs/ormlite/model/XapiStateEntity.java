package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
import com.ustadmobile.nanolrs.core.model.XapiAgentProxy;
import com.ustadmobile.nanolrs.core.model.XapiDocumentProxy;
import com.ustadmobile.nanolrs.core.model.XapiStateProxy;

/**
 * Created by mike on 10/2/16.
 */

@DatabaseTable(tableName = "xapi_state")
public class XapiStateEntity implements XapiStateProxy {

    public static final String COLNAME_CONTENT = "content";

    public static final String COLNAME_ACTIVITY = "activity";

    public static final String COLNAME_AGENT = "agent";

    public static final String COLNAME_REGISTRATION = "registration";

    public static final String COLNAME_DATESTORED = "date_stored";

    public static final String COLNAME_STATEID = "stateid";

    public static final String COLNAME_CONTENT_TYPE = "content_type";


    @DatabaseField(id = true)
    private String id;

    @DatabaseField(foreign = true, columnName =  COLNAME_ACTIVITY)
    private XapiActivityEntity activity;

    @DatabaseField(foreign = true, columnName =  COLNAME_AGENT)
    private XapiAgentEntity agent;

    @DatabaseField(index = true, columnName =  COLNAME_REGISTRATION)
    private String registration;

    @DatabaseField(index = true, columnName = COLNAME_STATEID)
    private String stateId;

    @DatabaseField(index = true, columnName =  COLNAME_DATESTORED)
    private long dateStored;

    @DatabaseField(foreign = true)
    private XapiDocumentEntity document;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = COLNAME_CONTENT)
    private byte[] content;

    @DatabaseField(columnName =  COLNAME_CONTENT_TYPE)
    private String contentType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public XapiActivityProxy getActivity() {
        return activity;
    }

    public void setActivity(XapiActivityProxy activity) {
        this.activity = (XapiActivityEntity)activity;
    }

    public XapiAgentProxy getAgent() {
        return agent;
    }

    public void setAgent(XapiAgentProxy agent) {
        this.agent = (XapiAgentEntity)agent;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public long getDateStored() {
        return dateStored;
    }

    public void setDateStored(long dateStored) {
        this.dateStored = dateStored;
    }

    @Override
    public XapiDocumentProxy getDocument() {
        return document;
    }

    @Override
    public void setDocument(XapiDocumentProxy document) {
        this.document = (XapiDocumentEntity)document;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
