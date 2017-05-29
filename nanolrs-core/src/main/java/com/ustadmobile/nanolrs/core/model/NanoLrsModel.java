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

    long getDateCreated();
    void setDateCreated(long dateCreated);

    long getDateStored();
    void setDateStored(long dateStored);

    long getDateModifiedAtMaster();
    void setDateModifiedAtMaster(long dateModifiedAtMaster);




}
