package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/9/2017.
 * If an entity's manager extends Syncable Manager, it means part of the sync process
 */

public interface NanoLrsManagerSyncable<T extends NanoLrsModelSyncable, P> extends NanoLrsManager {

    void persist(Object dbContext, NanoLrsModel data, boolean incrementChangeSeq)
            throws SQLException;

    /*
    Gets latest master sequence of this table
     */
    long getLatestMasterSequence(Object dbContext) throws SQLException;

    /**
     * Gets all entities in a list that have been modified since a particular
     * local sequence number. This number is usually gotten during sync
     * During sync:
     * We know which node we are to sent updates to . so we know the last latest local sequence number
     * for that entity that that node has from us.
     * We simply get everything on our end since that local seq number sent .
     * This value will be in sync_status's sent local seq number for that node and this entity.
     *
     * This method will give back all the new entities since that number.
     * If the entities were synced with master and they have a master seq set, we :
     *
     *
     * @param user
     * @param dbContext
     * @param host
     * @param seqNum
     * @return
     * @throws SQLException
     */
    List<NanoLrsModel> getAllSinceSequenceNumber(
            User user, Object dbContext, String host, long seqNum) throws SQLException;

    /**
     * Get all entities in a list between two seq numbers. For full documentation refer to :
     * getAllSinceSequenceNumber()
     *
     * @param user
     * @param host
     * @param fromSeqNum
     * @param toSeqNum
     * @param dbContext
     * @return
     * @throws SQLException
     */
    List<NanoLrsModel> getAllSinceTwoSequenceNumber(User user, String host,
        long fromSeqNum, long toSeqNum, Object dbContext) throws SQLException;

    /**
     * Get all entities in a list between two dates. This will search in date_created field.
     *
     * @param user
     * @param fromDate
     * @param toDate
     * @param dbContext
     * @return
     * @throws SQLException
     */
    List<NanoLrsModel> getAllSinceTwoDates(long fromDate, long toDate,
                                                    Object dbContext) throws SQLException;


}
