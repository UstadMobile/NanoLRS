package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.model.XapiAgent;

/**
 * Created by mike on 9/12/16.
 */
@DatabaseTable(tableName="xapi_activity")
public class XapiActivityEntity implements XapiActivity {

    @DatabaseField(id = true)
    String activityId;

    @DatabaseField(foreign = true)
    private XapiAgentEntity authority;

    @DatabaseField(dataType = DataType.LONG_STRING)
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
    public XapiAgent getAuthority() {
        return authority;
    }

    @Override
    public void setAuthority(XapiAgent authority) {
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
