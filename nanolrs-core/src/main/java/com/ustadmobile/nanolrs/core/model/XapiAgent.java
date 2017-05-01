package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/8/16.
 */
public interface XapiAgent extends NanoLrsModel {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    String getUuid();

    void setUuid(String uuid);

    XapiUser getUser();

    void setUser(XapiUser user);

    String getName();

    void setName(String name);

    String getMbox();

    void setMbox(String mbox);

    String getMboxSha1Sum();

    void setMboxSha1Sum(String mboxShe1Sum);

    String getOpenId();

    void setOpenId(String openId);

    String getOauthIdentifier();

    void setOauthIdentifier(String oAuthIdentifier);

    String getAccountHomepage();

    void setAccountHomepage(String accountHomepage);

    String getAccountName();

    void setAccountName(String accountName);

}
