package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiActivityManager {

    XapiActivityProxy findById(Object dbContext, String id);

    XapiActivityProxy makeNew(Object dbContext);

    void createOrUpdate(Object dbContext, XapiActivityProxy data);

}
