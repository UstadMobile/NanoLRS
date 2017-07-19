package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 6/9/2017.
 */


public interface ChangeSeq extends NanoLrsModel{

    String getUUID();
    void setUUID(String uuid);

    /**
     * Tells the generator that this is the primary key.
     *
     * @return
     * @nanolrs.primarykey
     */
    String getTable();
    void setTable(String table);

    long getNextChangeSeqNum();
    void setNextChangeSeqNum(long nextChangeSeqNum);

}
