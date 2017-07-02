package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/9/2017.
 * If an entity's manager extends Syncable Manager, it means part of the sync process
 */

public interface NanoLrsManagerSyncable<T extends NanoLrsModelSyncable, P> extends NanoLrsManager {


    /*
    Gets latest master sequence of this table
     */
    long getLatestMasterSequence(Object dbContext) throws SQLException;

    List<NanoLrsModel> getAllSinceSequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException;

    NanoLrsModelSyncable findAllRelatedToUser(Object dbContext, XapiUser user) throws SQLException;
}
