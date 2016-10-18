package com.ustadmobile.nanolrs.core.model;

import java.util.List;

/**
 * Created by mike on 10/6/16.
 */

public interface XapiAgentManager {

    List<XapiAgentProxy> findAgentByParams(Object dbContext, String mbox, String accountName, String accountHomepage);

    XapiAgentProxy makeNew(Object dbContext);

    void createOrUpdate(Object dbContext, XapiAgentProxy data);

}
