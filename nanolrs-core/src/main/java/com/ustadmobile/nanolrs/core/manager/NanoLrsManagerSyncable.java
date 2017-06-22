package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/9/2017.
 * If an entity's manager extends Syncable Manager, it means part of the sync process
 */

public interface NanoLrsManagerSyncable<T extends NanoLrsModel, P> extends NanoLrsManager {

    List<T> findBySequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException;
    List<NanoLrsModel> getAllSinceSequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException;


}
