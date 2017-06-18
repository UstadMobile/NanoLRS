package com.ustadmobile.nanolrs.test.core.model;

import com.ustadmobile.nanolrs.core.manager.RelationshipTest2StudentManager;
import com.ustadmobile.nanolrs.core.manager.RelationshipTest2TeacherManager;
import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.RelationshipTest2Student;
import com.ustadmobile.nanolrs.core.model.RelationshipTest2Teacher;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;


import java.util.List;
import java.util.UUID;

/**
 * Created by varuna on 6/12/2017.
 */

public class TestSequenceNumber {
    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatormTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();

        //Create some users
        XapiUserManager userManager = PersistenceManager.getInstance().getManager(XapiUserManager.class);

        XapiUser newUser = userManager.createSync(context, UUID.randomUUID().toString());
        newUser.setUsername("testuser");
        userManager.persist(context, newUser);

        XapiUser anotherNewUser = userManager.createSync(context, UUID.randomUUID().toString());
        anotherNewUser.setUsername("anothertestuser");
        userManager.persist(context, anotherNewUser);

        XapiUser newUser3 = userManager.createSync(context, UUID.randomUUID().toString());
        newUser3.setUsername("anothertestuser");
        userManager.persist(context, newUser3);

        XapiUser newUser4 = userManager.createSync(context, UUID.randomUUID().toString());
        newUser3.setUsername("anothertestuser");
        userManager.persist(context, newUser4);

        //create a relationship
        RelationshipTestManager relationshipTestManager =
                PersistenceManager.getInstance().getManager(RelationshipTestManager.class);
        RelationshipTest newRelationshipTest = (RelationshipTest) relationshipTestManager.makeNew();
        newRelationshipTest.setUuid(UUID.randomUUID().toString());
        newRelationshipTest.setOneUser(newUser);

        newRelationshipTest.setName("New Relationship");

        //Do we need to persist before m2m ? 
        //Update: Nope, we do not need to persist before
        //relationshipTestManager.persist(context, newRelationshipTest);


        //create an relationshipTest m2m with users (assign students)
        RelationshipTest2StudentManager relationshipTest2StudentManager =
                PersistenceManager.getInstance().getManager(RelationshipTest2StudentManager.class);
        RelationshipTest2Student newRelationshipTest2Student =
                (RelationshipTest2Student) relationshipTest2StudentManager.makeNew();
        newRelationshipTest2Student.setUuid(UUID.randomUUID().toString());
        newRelationshipTest2Student.setRelationshipTest(newRelationshipTest);
        newRelationshipTest2Student.setStudent(anotherNewUser);
        relationshipTest2StudentManager.persist(context, newRelationshipTest2Student );



        //newRelationshipTest.setTestStudents();

        RelationshipTest2TeacherManager relationshipTest2TeacherManager =
                PersistenceManager.getInstance().getManager(RelationshipTest2TeacherManager.class);
        RelationshipTest2Teacher newRelationshipTest2Teacher =
                (RelationshipTest2Teacher) relationshipTest2TeacherManager.makeNew();
        newRelationshipTest2Teacher.setUuid(UUID.randomUUID().toString());
        newRelationshipTest2Teacher.setRelationshipTest(newRelationshipTest);
        newRelationshipTest2Teacher.setTeacher(newUser3);
        relationshipTest2TeacherManager.persist(context, newRelationshipTest2Teacher);

        //newRelationshipTest.setTestTeachers();



        relationshipTestManager.persist(context, newRelationshipTest);

        long localSeqNumber = newRelationshipTest.getLocalSequence();
        Assert.assertNotNull(localSeqNumber);

        newRelationshipTest.setNotes("This is an update to the relationship..");
        relationshipTestManager.persist(context, newRelationshipTest);

        long newLocalSeqNumber = newRelationshipTest.getLocalSequence();

        Assert.assertEquals(localSeqNumber +1, newLocalSeqNumber);
    }
}
