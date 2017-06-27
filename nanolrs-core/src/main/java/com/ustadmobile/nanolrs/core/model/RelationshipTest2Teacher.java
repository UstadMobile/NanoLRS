package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

/**
 * Created by varuna on 5/27/2017.
 *
 * Many-to-Many relationship intermediary class
 */
public interface RelationshipTest2Teacher extends NanoLrsModelSyncable {
    /**
     * @return
     * @nanolrs.primarykey
     */
    @PrimaryKeyAnnotationClass(str = "pk")
    String getUuid();

    void setUuid(String id);

    /**
     * @nanolrs.foreignColumnName=uuid
     */
    RelationshipTest getRelationshipTest();
    void setRelationshipTest(RelationshipTest relationshipTest);

    XapiUser getTeacher();
    void setTeacher(XapiUser teacher);
}
