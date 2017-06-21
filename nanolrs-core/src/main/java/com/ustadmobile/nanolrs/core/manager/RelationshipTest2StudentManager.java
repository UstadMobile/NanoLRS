package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 6/13/2017.
 */

import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.util.List;

public interface RelationshipTest2StudentManager extends NanoLrsManagerSyncable {
    //TODO: This

    @Override
    List findBySequenceNumber(XapiUser user, Object dbContext, String host, long seqNum);

    @Override
    List getAllSinceSequenceNumber(XapiUser user, Object dbContext, String host, long seqNum);
}
