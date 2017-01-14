package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiVerb;

/**
 * Created by mike on 15/11/16.
 */

public interface XapiVerbManager {

    XapiVerb make(Object dbContext, String id);

    void persist(Object dbContext, XapiVerb data);

    XapiVerb findById(Object dbContext, String id);

}
