package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiAgent;

import java.util.List;

/**
 * Created by mike on 10/6/16.
 */

public interface XapiAgentManager {

    List<XapiAgent> findAgentByParams(Object dbContext, String mbox, String accountName, String accountHomepage);

    XapiAgent makeNew(Object dbContext);

    void createOrUpdate(Object dbContext, XapiAgent data);

}
