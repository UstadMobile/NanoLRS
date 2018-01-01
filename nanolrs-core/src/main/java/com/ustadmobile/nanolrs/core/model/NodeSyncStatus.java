package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 12/14/2017.
 */

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

/**
 * This model is used by UMSync Process.
 * Its primary objective is to store all sync attempts with
 * a particular Node and its date along with the sync result.
 * We use this to show the last time the app synced with
 * master and we use it to show the sync attempts as well.
 */
public interface NodeSyncStatus extends NanoLrsModel {
    /**
     * Tells the generator that this is the primary key.
     *
     * @return
     * @nanolrs.primarykey
     */
    String getUUID();
    void setUUID(String uuid);

    /* Whom the sync with was */
    String getHost();
    void setHost(String host);

    /* The node with which the sync happened */
    Node getNode();
    void setNode(Node node);

    /* The date of this sync */
    long getSyncDate();
    void setSyncDate(long syncDate);

    /* The result of this sync:
    In response code + comments (SUCCESS, FAIL, etc) */
    String getSyncResult();
    void setSyncResult(String syncResult);
}
