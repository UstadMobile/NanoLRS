package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;

import java.sql.SQLException;

public interface SyncStatusManager extends NanoLrsManager {
    long getSentStatus(String host, Class entity, Object dbContext) throws SQLException;
    boolean updateSyncStatusSeqNum(String host, Class entity, long newSentSeq, long newRecSeq, Object dbContext) throws SQLException;
    NanoLrsModel getSyncStatus(String host, Class entity, Object dbContext) throws SQLException;

}
