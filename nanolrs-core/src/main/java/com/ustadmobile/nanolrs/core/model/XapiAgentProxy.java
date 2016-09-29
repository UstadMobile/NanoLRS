package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/8/16.
 */
public interface XapiAgentProxy {

    String getId();

    void setId(String id);

    XapiUserProxy getUser();

    void setUser(XapiUserProxy user);

    String getName();

    void setName(String name);

    String getMbox();

    void setMbox(String mbox);

    String getMboxSha1Sum();

    void setMboxSha1Sum(String mboxShe1Sum);

    String getOpenId();

    void setOpenId(String openId);

    String getoAuthIdentifier();

    void setoAuthIdentifier(String oAuthIdentifier);

    String getAccountHomepage();

    void setAccountHomepage(String accountHomepage);

    String getAccountName();

    void setAccountName(String accountName);

}
