package com.ustadmobile.nanolrs.core.model;

import java.util.List;

/**
 * Created by mike on 9/27/16.
 */

public interface XapiUserManager {

    XapiUserProxy createSync(Object dbContext, String id);

    void persist(Object dbContext, XapiUserProxy user);

    XapiUserProxy findById(Object dbContext, String id);

    List<XapiUserProxy> findByUsername(Object dbContext, String username);

    void delete(Object dbContext, XapiUserProxy data);


}
