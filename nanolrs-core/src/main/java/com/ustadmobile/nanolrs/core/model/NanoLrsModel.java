package com.ustadmobile.nanolrs.core.model;

//import com.j256.ormlite.field.types.DateTimeType;

/**
 * Created by mike on 4/30/17.
 */

public interface NanoLrsModel {


    String getNotes();
    void setNotes(String notes);

    String getLocalSequence();
    void setLocalSequence(String localSequence);

    String getMasterSequence();
    void setMasterSequence(String masterSequence);

    /* The date stored at source. Be it on server, client, etc.
    While creation, this is auto value = now
    During sync, we override that with the value in the sync
    During conflict, we get the latest one (as usual) - might have extra changes
    Does NOT get updated. Stays fixed
     */
    long getDateCreated();
    void setDateCreated(long dateCreated);

    long getDateStored();
    void setDateStored(long dateStored);

    long getDateModifiedAtMaster();
    void setDateModifiedAtMaster(long dateModifiedAtMaster);




}
