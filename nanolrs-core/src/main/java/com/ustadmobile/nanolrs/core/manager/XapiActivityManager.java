package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiActivity;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiActivityManager {

    XapiActivity findById(Object dbContext, String id);

    XapiActivity makeNew(Object dbContext);

    void createOrUpdate(Object dbContext, XapiActivity data);

    void deleteById(Object dbContext, String id);

}
