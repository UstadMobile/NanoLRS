package com.ustadmobile.nanolrs.ormlite.model;

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

    @DatabaseField(id = true)
    private String id;

    @DatabaseField(foreign = true)
    private XapiActivityEntity activity;

    @DatabaseField(foreign = true)
    private XapiAgentEntity agent;

    @DatabaseField(index = true)
    private String registration;

    @DatabaseField
    private String stateId;

    @DatabaseField
    private long dateStored;

    @DatabaseField(foreign = true)
    private XapiDocumentEntity document;


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
}
