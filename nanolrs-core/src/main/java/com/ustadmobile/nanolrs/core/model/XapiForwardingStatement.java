package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/13/16.
 */
public interface XapiForwardingStatement extends NanoLrsModel {


    public static final int STATUS_QUEUED = 1;

    public static final int STATUS_TRYAGAIN = 2;

    public static final int STATUS_SENT = 3;

    public static final int STATUS_FAILED = 4;

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    String getUuid();

    void setUuid(String uuid);

    XapiStatement getStatement();

    void setStatement(XapiStatement statement);

    String getDestinationURL();

    void setDestinationURL(String destinationURL);

    String getHttpAuthUser();

    void setHttpAuthUser(String httpAuthUser);

    String getHttpAuthPassword();

    void setHttpAuthPassword(String httpAuthPassword);

    int getStatus();

    void setStatus(int status);

    int getTryCount();

    void setTryCount(int tryCount);

    long getTimeSent();

    void setTimeSent(long timeSent);

}
