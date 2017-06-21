package com.ustadmobile.nanolrs.test.core.model;
/**
 * Created by varuna on 6/21/2017.
 */

import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class TestGetEntitiesSinceSequenceNumber {
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

        newRelationshipTest.setName("New Relationship");
        relationshipTestManager.persist(context, newRelationshipTest, relationshipTestManager);
        
        //another 1
        RelationshipTest newRelationshipTest2 =
                (RelationshipTest) relationshipTestManager.makeNew();
        newRelationshipTest2.setUuid(UUID.randomUUID().toString());
        newRelationshipTest2.setOneUser(newUser);

        newRelationshipTest2.setName("New Relationship");
        relationshipTestManager.persist(context, newRelationshipTest2, relationshipTestManager);
        
        //Get all entities since sequence number 0
        long sequenceNumber = 0;
        XapiUser currentUser = null;
        String host = "testing_host";

        List allRelationshipTestsSince = relationshipTestManager.getAllSinceSequenceNumber(
                currentUser, context, host, sequenceNumber);
        Assert.assertNotNull(allRelationshipTestsSince);
        Assert.assertEquals(allRelationshipTestsSince.size(), 3);

    }
}
