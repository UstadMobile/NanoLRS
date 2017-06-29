package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;

import java.sql.SQLException;

public interface ChangeSeqManager extends NanoLrsManagerSyncable {
    /**
     * Gets the nextChangeSeqNumber for table name (string)
     * @param tableName
     * @param dbContext
     * @return
     * @throws SQLException
     */
    long getNextChangeByTableName(String tableName, Object dbContext) throws SQLException;

    /**
     * Updates the nextChange Seq Number + increment for ChangeSeq table
     * @param tableName
     * @param increment
     * @param dbContext
     * @throws SQLException
     */
    long getNextChangeAddSeqByTableName(String tableName, int increment, Object dbContext)
            throws SQLException;
}
