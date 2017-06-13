package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 5/26/2017.
 */

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

import java.util.Collection;

public interface RelationshipTest extends NanoLrsModel {

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

    //UPDATE: Not doing this, as I'd have to add something on XapiUser's side
    //why- because its a one2m relationshp that means one XapiUser can be in
    //only one RelationshipTest (another analogy: Mother to Many children.
    //Every child has only one mother. So On XapiUser, there is a foreignKey
    //which I wont change, since this is just a test.
    //Users list: One to Many (reverse Foreignkey) - Another Entity
    /**
     * Not using foreignFieldName here. Relationship is only one sided.
     * nanolrs.foreignFieldName=listtest
     */
    //Collection<? extends XapiUser> getUsers();
    //void setUsers(Collection<? extends XapiUser> users);

    /**
     * foreignFieldName=student //Disabling
     */
    Collection<? extends RelationshipTest2Student> getTestStudents();
    void setTestStudents(Collection<? extends RelationshipTest2Student> testStudents);

    /**
     * foreignFieldName=teacher //Disabling
     */
    Collection<? extends RelationshipTest2Teacher> getTestTeachers();
    void setTestTeachers(Collection<? extends RelationshipTest2Teacher> testTeachers);

}
