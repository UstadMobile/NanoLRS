package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 6/9/2017.
 */

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

public interface SyncStatus {
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

    String getTable();
    void setTable(String table);

    String getSentSeq();
    void setSentSeq(String sentSeq);

    String getReceivedSeq();
    void setReceivedSeq(String receivedSeq);

}
