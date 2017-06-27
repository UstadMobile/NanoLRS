package com.ustadmobile.nanolrs.test.core.util;
/**
 * Created by varuna on 6/22/2017.
 */

import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class TestEntitiesToJsonArray {
    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();

        //Create some users
        XapiUserManager userManager =
                PersistenceManager.getInstance().getManager(XapiUserManager.class);

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
        RelationshipTest newRelationshipTest =
                (RelationshipTest) relationshipTestManager.makeNew();
        newRelationshipTest.setUuid(UUID.randomUUID().toString());
        newRelationshipTest.setOneUser(newUser);

        newRelationshipTest.setName("New Relationship1");
        relationshipTestManager.persist(context, newRelationshipTest);

        //another 1
        RelationshipTest newRelationshipTest2 =
                (RelationshipTest) relationshipTestManager.makeNew();
        newRelationshipTest2.setUuid(UUID.randomUUID().toString());
        newRelationshipTest2.setOneUser(newUser);

        newRelationshipTest2.setName("New Relationship2");
        relationshipTestManager.persist(context, newRelationshipTest2);

        //another 1
        RelationshipTest newRelationshipTest3 =
                (RelationshipTest) relationshipTestManager.makeNew();
        newRelationshipTest3.setUuid(UUID.randomUUID().toString());
        newRelationshipTest3.setOneUser(newUser);

        newRelationshipTest3.setName("New Relationship3");
        relationshipTestManager.persist(context, newRelationshipTest3);

        //Get all entities since sequence number 0
        long sequenceNumber = 0;
        XapiUser currentUser = null;
        String host = "testing_host";

        /* Manually change master seq so that we get the right statmenets that need to be sent */
        newRelationshipTest.setMasterSequence(2);
        relationshipTestManager.persist(context, newRelationshipTest);
        newRelationshipTest3.setMasterSequence(1);
        relationshipTestManager.persist(context, newRelationshipTest3);
        newRelationshipTest2.setMasterSequence(3);
        relationshipTestManager.persist(context, newRelationshipTest2);

        /* Test every statmente from seq 1 after change in master seq */


        sequenceNumber=1;
        List<NanoLrsModel> allRelationshipTestSince2 =
                relationshipTestManager.getAllSinceSequenceNumber(currentUser, context,
                        host, sequenceNumber);
        Assert.assertNotNull(allRelationshipTestSince2);
        Assert.assertEquals(allRelationshipTestSince2.size(), 4);


        RelationshipTest testThisEntity = (RelationshipTest) allRelationshipTestSince2.get(0);
        /*
        JSONObject thisEntityJson = ProxyJsonSerializer.toJson(
                testThisEntity, RelationshipTest.class);
        Assert.assertNotNull(thisEntityJson);
        */

    }
}
