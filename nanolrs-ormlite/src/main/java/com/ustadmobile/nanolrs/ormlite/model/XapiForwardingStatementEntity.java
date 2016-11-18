package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementProxy;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;

/**
 * Created by mike on 9/13/16.
 */
@DatabaseTable(tableName = "xapi_forwarding_statements")
public class XapiForwardingStatementEntity implements XapiForwardingStatementProxy{

    public static final String FIELD_NAME_STATUS = "status";

    public static final String FIELD_NAME_STATEMENT = "statement_id";

    @DatabaseField(id = true)
    String uuid;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    XapiStatementEntity statement;

    @DatabaseField
    String destinationURL;

    @DatabaseField
    String httpAuthUser;

    @DatabaseField
    String httpAuthPassword;

    @DatabaseField(columnName =  FIELD_NAME_STATUS)
    int status;

    @DatabaseField
    int tryCount;

    @DatabaseField
    long timeSent;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public void setStatement(XapiStatementProxy statement) {
        this.statement = (XapiStatementEntity)statement;
    }

    @Override
    public XapiStatementProxy getStatement() {
        return statement;
    }

    @Override
    public String getDestinationURL() {
        return destinationURL;
    }

    @Override
    public void setDestinationURL(String destinationURL) {
        this.destinationURL = destinationURL;
    }

    @Override
    public String getHttpAuthUser() {
        return httpAuthUser;
    }

    @Override
    public void setHttpAuthUser(String httpAuthUser) {
        this.httpAuthUser = httpAuthUser;
    }

    @Override
    public String getHttpAuthPassword() {
        return httpAuthPassword;
    }

    @Override
    public void setHttpAuthPassword(String httpAuthPassword) {
        this.httpAuthPassword = httpAuthPassword;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getTryCount() {
        return tryCount;
    }

    @Override
    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    @Override
    public long getTimeSent() {
        return timeSent;
    }

    @Override
    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }
}
