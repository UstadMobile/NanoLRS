package com.ustadmobile.nanolrs.test.core.model;

import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by varuna on 6/29/2017.
 */

public class TestSetup {

    public void setup() throws Exception{

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
        RelationshipTest newRelationshipTest1 =
                (RelationshipTest) relationshipTestManager.makeNew();
        newRelationshipTest1.setUuid(UUID.randomUUID().toString());
        newRelationshipTest1.setOneUser(newUser);

        newRelationshipTest1.setName("New Relationship1");
        relationshipTestManager.persist(context, newRelationshipTest1);

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
    }
}
