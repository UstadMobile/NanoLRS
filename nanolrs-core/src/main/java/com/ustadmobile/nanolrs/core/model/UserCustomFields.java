package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 7/26/2017.
 */

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

public interface UserCustomFields extends NanoLrsModelSyncable {
    /**
     * Tells the generator that this is the primary key.
     *
     * @return
     * @nanolrs.primarykey
     */
    @PrimaryKeyAnnotationClass(str="pk")
    String getUuid();
    void setUuid(String uuid);

    User getUser();
    void setUser(User user);

    int getFieldName();
    void setFieldName(int fieldName);

    String getFieldValue();
    void setFieldValue(String fieldValue);

}
