package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 6/9/2017.
 */

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

public interface ChangeSeq {
    /**
     * Tells the generator that this is the primary key.
     *
     * @return
     * @nanolrs.primarykey
     */
    String getUUID();

    void setUUID(String uuid);

    String getTable();
    void setTable(String table);

    String getNextChangeSeqNum();
    void setNextChangeSeqNum(String nextChangeSeqNum);


}
