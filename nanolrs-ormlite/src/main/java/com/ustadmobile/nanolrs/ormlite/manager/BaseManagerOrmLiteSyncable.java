package com.ustadmobile.nanolrs.ormlite.manager;

import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/21/2017.
 */

public abstract class BaseManagerOrmLiteSyncable<T extends NanoLrsModelSyncable, P>
        extends BaseManagerOrmLite implements NanoLrsManagerSyncable<T,P> {
    @Override
    public List<T> findBySequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException {
        return null;
    }

    @Override
    public List<NanoLrsModel> getAllSinceSequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException {

        return null;
    }
}
