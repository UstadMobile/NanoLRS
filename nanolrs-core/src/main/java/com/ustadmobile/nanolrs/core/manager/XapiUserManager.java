package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.util.List;

/**
 * Created by mike on 9/27/16.
 */

public interface XapiUserManager extends NanoLrsManagerSyncable {

    XapiUser createSync(Object dbContext, String id);

    XapiUser findById(Object dbContext, String id);

    List<XapiUser> findByUsername(Object dbContext, String username);

    void delete(Object dbContext, XapiUser data);


}
