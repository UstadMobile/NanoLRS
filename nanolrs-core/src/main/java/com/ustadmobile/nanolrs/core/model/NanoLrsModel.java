package com.ustadmobile.nanolrs.core.model;

import com.j256.ormlite.field.types.DateTimeType;

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

    DateTimeType getDateCreated();
    void setDateCreated(DateTimeType dateCreated);

    DateTimeType getDateStored();
    void setDateStored(DateTimeType dateStored);

    DateTimeType getDateModifiedAtMaster();
    void setDateModifiedAtMaster(DateTimeType dateModifiedAtMaster);




}
