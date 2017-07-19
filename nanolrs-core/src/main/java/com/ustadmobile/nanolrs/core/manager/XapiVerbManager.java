package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiVerb;

/**
 * Created by mike on 15/11/16.
 */

public interface XapiVerbManager extends NanoLrsManagerSyncable{

    XapiVerb make(Object dbContext, String verbId);

    void persist(Object dbContext, XapiVerb data);

    XapiVerb findById(Object dbContext, String id);

}
