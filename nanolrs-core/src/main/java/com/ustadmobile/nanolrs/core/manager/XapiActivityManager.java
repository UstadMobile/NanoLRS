package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.model.XapiAgent;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 10/2/16.
 */

public interface XapiActivityManager extends NanoLrsManagerSyncable {

    XapiActivity findByActivityId(Object dbContext, String id);

    XapiActivity makeNew(Object dbContext);

    void createOrUpdate(Object dbContext, XapiActivity data);

    void deleteByActivityId(Object dbContext, String id);

    List<XapiActivity> findByAuthority(Object dbContext, XapiAgent agent) throws SQLException;

}
