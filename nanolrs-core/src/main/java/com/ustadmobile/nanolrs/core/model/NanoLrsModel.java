package com.ustadmobile.nanolrs.core.model;

//import com.j256.ormlite.field.types.DateTimeType;

/**
 * Created by mike on 4/30/17.
 */

public interface NanoLrsModel {


    /**
     * This denotes any extra notes we may have for this entry of this entity.
     */
    String getNotes();
    void setNotes(String notes);

    /**
     * This denotes the time this entry for this entity was made locally.
     * Changed name from dateStored to storedDate cause
     * there is a variable what that name in XapiState. Ignoring.
     */
    long getStoredDate();
    void setStoredDate(long storedDate);


}
