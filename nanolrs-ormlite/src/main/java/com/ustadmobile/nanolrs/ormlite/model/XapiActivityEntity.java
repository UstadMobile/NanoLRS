package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
import com.ustadmobile.nanolrs.core.model.XapiAgentProxy;

/**
 * Created by mike on 9/12/16.
 */
@DatabaseTable(tableName="xapi_activity")
public class XapiActivityEntity implements XapiActivityProxy{

    @DatabaseField(id = true)
    String activityId;

    @DatabaseField(foreign = true)
    private XapiAgentEntity authority;

    @DatabaseField
    private String canonicalData;

    @Override
    public String getActivityId() {
        return activityId;
    }

    @Override
    public void setActivityId(String id) {
        this.activityId = id;
    }

    @Override
    public XapiAgentProxy getAuthority() {
        return authority;
    }

    @Override
    public void setAuthority(XapiAgentProxy authority) {
        this.authority = (XapiAgentEntity)authority;
    }

    @Override
    public String getCanonicalData() {
        return canonicalData;
    }

    @Override
    public void setCanonicalData(String canonicalData) {
        this.canonicalData = canonicalData;
    }
}
