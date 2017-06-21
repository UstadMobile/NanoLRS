package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.util.List;

/**
 * Created by varuna on 6/13/2017.
 */

public interface RelationshipTest2TeacherManager extends NanoLrsManagerSyncable {
    //TODO: This

    @Override
    List findBySequenceNumber(XapiUser user, Object dbContext, String host, long seqNum);

    @Override
    List getAllSinceSequenceNumber(XapiUser user, Object dbContext, String host, long seqNum);
}
