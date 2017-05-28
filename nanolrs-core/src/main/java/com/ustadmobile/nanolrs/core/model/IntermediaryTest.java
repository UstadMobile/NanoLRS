package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

/**
 * Created by varuna on 5/27/2017.
 *
 * Many-to-Many relationship intermediary class
 */
public interface IntermediaryTest extends NanoLrsModel {
    /**
     * @return
     * @nanolrs.primarykey
     */
    @PrimaryKeyAnnotationClass(str = "pk")
    String getUuid();

    void setUuid(String id);

    XapiUser getStudent();

    void setStudent(XapiUser student);

    XapiUser getTeacher();

    void setTeacher(XapiUser teacher);
}
