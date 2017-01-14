package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiUser;

/**
 * Created by mike on 9/12/16.
 */
@DatabaseTable(tableName="xapi_agents")
public class XapiAgentEntity implements XapiAgent {

    public static final String COLNAME_MBOX = "mbox";

    public static final String COLNAME_ACCOUNT_NAME = "account_name";

    public static final String COLNAME_ACCOUNT_HOMEPAGE = "account_homepage";

    @DatabaseField(id =  true)
    private String id;

    @DatabaseField
    private String name;

    @DatabaseField(columnName = COLNAME_MBOX)
    private String mbox;

    @DatabaseField
    private String mboxSha1Sum;

    @DatabaseField
    private String openId;

    @DatabaseField
    private String oAuthIdentifier;

    @DatabaseField(columnName =  COLNAME_ACCOUNT_HOMEPAGE)
    private String accountHomepage;

    @DatabaseField(columnName = COLNAME_ACCOUNT_NAME)
    private String accountName;

    @DatabaseField(foreign =  true)
    private XapiUserEntity user;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getMbox() {
        return mbox;
    }

    @Override
    public void setMbox(String mbox) {
        this.mbox = mbox;
    }

    @Override
    public String getMboxSha1Sum() {
        return mboxSha1Sum;
    }

    @Override
    public void setMboxSha1Sum(String mboxSha1Sum) {
        this.mboxSha1Sum = mboxSha1Sum;
    }

    @Override
    public String getOpenId() {
        return openId;
    }

    @Override
    public void setOpenId(String openId) {
        this.openId = openId;
    }

    @Override
    public String getoAuthIdentifier() {
        return oAuthIdentifier;
    }

    @Override
    public void setoAuthIdentifier(String oAuthIdentifier) {
        this.oAuthIdentifier = oAuthIdentifier;
    }

    @Override
    public String getAccountHomepage() {
        return accountHomepage;
    }

    @Override
    public void setAccountHomepage(String accountHomepage) {
        this.accountHomepage = accountHomepage;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public XapiUser getUser() {
        return user;
    }

    @Override
    public void setUser(XapiUser user) {
        this.user = (XapiUserEntity)user;
    }
}
