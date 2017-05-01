package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.util.List;

/**
 * Created by mike on 9/27/16.
 */

public interface XapiUserManager extends NanoLrsManager {

    XapiUser createSync(Object dbContext, String id);

    void persist(Object dbContext, XapiUser user);

    XapiUser findById(Object dbContext, String id);

    List<XapiUser> findByUsername(Object dbContext, String username);

    void delete(Object dbContext, XapiUser data);


}
