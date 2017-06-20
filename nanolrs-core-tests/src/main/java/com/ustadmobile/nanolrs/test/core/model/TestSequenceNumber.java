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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
        anotherNewUser.setUsername("testuser2");
        userManager.persist(context, anotherNewUser);

        XapiUser newUser3 = userManager.createSync(context, UUID.randomUUID().toString());
        newUser3.setUsername("testuser3");
        userManager.persist(context, newUser3);

        XapiUser newUser4 = userManager.createSync(context, UUID.randomUUID().toString());
        newUser4.setUsername("testuser4");
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
        newRelationshipTest2Student.setStudent(newUser);
        relationshipTest2StudentManager.persist(context, newRelationshipTest2Student );

        //another student..
        RelationshipTest2StudentManager relationshipTest2StudentManager2 =
                PersistenceManager.getInstance().getManager(RelationshipTest2StudentManager.class);
        RelationshipTest2Student newRelationshipTest2Student2 =
                (RelationshipTest2Student) relationshipTest2StudentManager2.makeNew();
        newRelationshipTest2Student2.setUuid(UUID.randomUUID().toString());
        newRelationshipTest2Student2.setRelationshipTest(newRelationshipTest);
        newRelationshipTest2Student2.setStudent(anotherNewUser);
        relationshipTest2StudentManager2.persist(context, newRelationshipTest2Student2 );


        //Creating a relationshipTest m2m with users(assign teachers)
        RelationshipTest2TeacherManager relationshipTest2TeacherManager =
                PersistenceManager.getInstance().getManager(RelationshipTest2TeacherManager.class);
        RelationshipTest2Teacher newRelationshipTest2Teacher =
                (RelationshipTest2Teacher) relationshipTest2TeacherManager.makeNew();
        newRelationshipTest2Teacher.setUuid(UUID.randomUUID().toString());
        newRelationshipTest2Teacher.setRelationshipTest(newRelationshipTest);
        newRelationshipTest2Teacher.setTeacher(newUser3);
        relationshipTest2TeacherManager.persist(context, newRelationshipTest2Teacher);

        //another teacher..
        RelationshipTest2TeacherManager relationshipTest2TeacherManager2 =
                PersistenceManager.getInstance().getManager(RelationshipTest2TeacherManager.class);
        RelationshipTest2Teacher newRelationshipTest2Teacher2 =
                (RelationshipTest2Teacher) relationshipTest2TeacherManager2.makeNew();
        newRelationshipTest2Teacher2.setUuid(UUID.randomUUID().toString());
        newRelationshipTest2Teacher2.setRelationshipTest(newRelationshipTest);
        newRelationshipTest2Teacher2.setTeacher(newUser4);
        relationshipTest2TeacherManager2.persist(context, newRelationshipTest2Teacher2);

        //relationshipTestManager.persist(context, newRelationshipTest);
        relationshipTestManager.persist(context, newRelationshipTest, relationshipTestManager);
        long localSeqNumber = newRelationshipTest.getLocalSequence();
        Assert.assertNotNull(localSeqNumber);

        newRelationshipTest.setNotes("This is an update to the relationship..");
        //relationshipTestManager.persist(context, newRelationshipTest);
        relationshipTestManager.persist(context, newRelationshipTest, relationshipTestManager);
        long newLocalSeqNumber = newRelationshipTest.getLocalSequence();
        Assert.assertEquals(localSeqNumber +1, newLocalSeqNumber);

        RelationshipTest theNewRelationshipTest = (RelationshipTest)
                relationshipTestManager.findByPrimaryKey(context, newRelationshipTest.getUuid());
        Collection<? extends RelationshipTest2Student> rtStudents =
                theNewRelationshipTest.getTestStudents();
        Collection<? extends RelationshipTest2Teacher> rtTeachers =
                theNewRelationshipTest.getTestTeachers();

        Assert.assertEquals(rtStudents.isEmpty(), false);
        Assert.assertEquals(rtTeachers.isEmpty(), false);
        Assert.assertEquals(rtStudents.size(), 2);
        Assert.assertEquals(rtTeachers.size(), 2);

        List<String> studentUsernames = new ArrayList<String>();
        Iterator<? extends RelationshipTest2Student> studentsIterator = rtStudents.iterator();
        while(studentsIterator.hasNext()){
            XapiUser student = studentsIterator.next().getStudent();
            String studentUsername = student.getUsername();
            studentUsernames.add(studentUsername);
        }
        Assert.assertTrue(studentUsernames.contains("testuser"));
        Assert.assertTrue(studentUsernames.contains("testuser2"));

        List<String> teacherUsernames = new ArrayList<String>();
        Iterator<? extends RelationshipTest2Teacher> teachersIterator = rtTeachers.iterator();
        while(teachersIterator.hasNext()){
            XapiUser teacher = teachersIterator.next().getTeacher();
            String teacherUsername = teacher.getUsername();
            teacherUsernames.add(teacherUsername);
        }
        Assert.assertTrue(teacherUsernames.contains("testuser3"));
        Assert.assertTrue(teacherUsernames.contains("testuser4"));



    }
}
