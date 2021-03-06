package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 6/9/2017.
 */

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

/**
 * This Model is used in UMSync Process. Its primary role is
 * to save sent and received (not implemented yet) statuses
 * with every syncable table.
 */
public interface SyncStatus extends NanoLrsModel{
    /**
     * Tells the generator that this is the primary key.
     *
     * @return
     * @nanolrs.primarykey
     */
    String getUUID();

    void setUUID(String uuid);

    String getHost();
    void setHost(String host);

    /*
    Node getNode();
    void setNode(Node node);
    */

    String getTable();
    void setTable(String table);

    long getSentSeq();
    void setSentSeq(long sentSeq);

    long getReceivedSeq();
    void setReceivedSeq(long receivedSeq);

}
