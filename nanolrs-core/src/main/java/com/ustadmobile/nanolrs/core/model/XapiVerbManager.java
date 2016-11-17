package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 15/11/16.
 */

public interface XapiVerbManager {

    XapiVerbProxy make(Object dbContext, String id);

    void persist(Object dbContext, XapiVerbProxy data);

    XapiVerbProxy findById(Object dbContext, String id);

}
