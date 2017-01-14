package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiUser;

/**
 * Created by mike on 9/27/16.
 */
@DatabaseTable(tableName="xapi_users")
public class XapiUserEntity implements XapiUser {

    public static final String COLNAME_USERNAME = "username";

    @DatabaseField(id = true)
    public String id;

    @DatabaseField(index =  true, columnName = COLNAME_USERNAME)
    public String username;

    @DatabaseField
    public String password;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
}
