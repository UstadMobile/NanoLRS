package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 5/26/2017.
 */

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

import java.util.Collection;
import java.util.List;

public interface ListTest extends NanoLrsModel {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    @PrimaryKeyAnnotationClass(str="pk")
    String getUuid();

    void setUuid(String id);

    //String Primitive field
    public String getName();
    public void setName(String username);

    //Foreign Key : Non primitive field - Another entity
    XapiUser getOneUser();
    void setOneUser(XapiUser oneUser);

    //field as a list (no relationship)
    //This doesn't really work. You can't have an array as a field in the DB
    //List<String> getNames();
    //void setNames(List<String> names);

    //Users list: One to Many (reverse Foreignkey) - Another Entity
    /**
     * Not using foreignFieldName here. Relationship is only one sided.
     * nanolrs.foreignFieldName=listtest
     */
    Collection<? extends XapiUser> getUsers();
    void setUsers(Collection<? extends XapiUser> users);


    /**
     * @nanolrs.foreignFieldName=student
     */
    Collection<? extends IntermediaryTest> getTestStudents();
    void setTestStudents(Collection<? extends IntermediaryTest> testStudents);

    /**
     * @nanolrs.foreignFieldName=teacher
     */
    Collection<? extends IntermediaryTest> getTestTeachers();
    void setTestTeachers(Collection<? extends IntermediaryTest> testTeachers);

}
