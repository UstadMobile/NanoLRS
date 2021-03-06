package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

/**
 * Created by mike on 9/8/16.
 */
public interface XapiAgent extends NanoLrsModelSyncable {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    @PrimaryKeyAnnotationClass(str="pk")
    String getUuid();

    void setUuid(String uuid);

    User getUser();

    void setUser(User user);

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
