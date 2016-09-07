package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;

/**
 * Created by mike on 9/6/16.
 */
@DatabaseTable(tableName="xapistatements")
public class XapiStatementEntity implements XapiStatementProxy {

    @DatabaseField(id =  true)
    private String uuid;

    @DatabaseField
    private long timestamp;

    @DatabaseField
    private String contextRegistration;

    public String getContextRegistration() {
        return contextRegistration;
    }

    public void setContextRegistration(String contextRegistration) {
        this.contextRegistration = contextRegistration;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



}
