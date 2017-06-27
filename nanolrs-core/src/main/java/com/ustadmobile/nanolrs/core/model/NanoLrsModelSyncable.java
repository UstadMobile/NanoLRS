package com.ustadmobile.nanolrs.core.model;

import java.util.List;

/**
 * Created by varuna on 6/11/2017.
 */

public interface NanoLrsModelSyncable extends NanoLrsModel {

    /**
     * local_sequence is the sequence Number locally. This will always get a +1
     *  upon being saved (persisted) locally. We keep this for internal to the
     *  UM instance use.
     */
    long getLocalSequence();
    void setLocalSequence(long localSequence);

    /**
     * master_sequence is the sequence number at master. Under the assumption that
     * there is always a since master server. This will be in sync with the main
     * master server and will be accordingly updated according to UMSync endpoint
     * A change in mastersequence denotes an update to this specific entry of this
     * entity.
     */
    long getMasterSequence();
    void setMasterSequence(long masterSequence);

    /**
     *  The date stored at source. Be it on server, client, etc.
     *  While creation, this is auto value = now
     *  During sync, we override that with the value in the sync
     *  During conflict, we get the latest one (as usual) - might have extra changes
     *  Does NOT get updated. Stays fixed
     */
    long getDateCreated();
    void setDateCreated(long dateCreated);

    /**
     * This denotes the date this entry of this entity was modified at master.
     * This can be used to resolve conflicts or for any other operation where
     * this can be useful.
     */
    long getDateModifiedAtMaster();
    void setDateModifiedAtMaster(long dateModifiedAtMaster);
}
