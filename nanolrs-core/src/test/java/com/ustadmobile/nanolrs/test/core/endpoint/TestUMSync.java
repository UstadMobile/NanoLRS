package com.ustadmobile.nanolrs.test.core.endpoint;
/**
 * Created by varuna on 7/20/2017.
 */

import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class TestUMSync {

    public static Object context;

    public static Object endpointContext;


    @Before
    public void setUp() throws Exception{
        endpointContext = NanoLrsPlatformTestUtil.getSyncEndpointContext();
        context = NanoLrsPlatformTestUtil.getContext();
        PersistenceManager.getInstance().forceInit(endpointContext);
        PersistenceManager.getInstance().forceInit(context);
    }

    @Test
    public void testLifecycle() throws Exception {
        //Get the endpoint connectionSource from platform db pool
        if(endpointContext == null) {
            Object endpointContext = NanoLrsPlatformTestUtil.getSyncEndpointContext();
        }

        //Create an endpoint server
        NanoLrsHttpd httpd = new NanoLrsHttpd(0, endpointContext);

        //Start the server
        httpd.start();
        httpd.mapSyncEndpoint("/sync");
        int serverPort = httpd.getListeningPort();
        String endpointUrl = "http://localhost:" + serverPort + "/sync";

        //Managers
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        ChangeSeqManager changeSeqManager = PersistenceManager.getInstance().getManager(
                ChangeSeqManager.class);

        //Get initial seq number for user table - for debugging purposes
        String tableName = "USER";
        long initialSeqNum =
                changeSeqManager.getNextChangeByTableName(tableName, context) -1;

        ///Create this testing user: testinguser
        //Use it for Sync purposes. Assign it roles and
        //users for testing user specific syncing.
        String newTestingUserID = UUID.randomUUID().toString();
        User testingUser = (User)userManager.makeNew();
        testingUser.setUuid(newTestingUserID);
        testingUser.setUsername("newtestinguser");
        testingUser.setPassword("secret");
        userManager.persist(context, testingUser);

        //Create this node
        Node thisNode = (Node) nodeManager.makeNew();
        thisNode.setRole("this_node");
        thisNode.setUUID(UUID.randomUUID().toString());
        thisNode.setHost("client");
        thisNode.setUrl("http://loclhost:4242");
        nodeManager.persist(context, thisNode);

        ///Create a node for testing
        Node testingNode = (Node) nodeManager.makeNew();
        testingNode.setUUID(UUID.randomUUID().toString());
        testingNode.setUrl(endpointUrl);
        testingNode.setHost("testhost");
        testingNode.setName("Testing node");
        testingNode.setRole("tester");
        nodeManager.persist(context, testingNode);


        //pasting

        //Get number of users already in system
        int initialUserCount = userManager.getAll(context).size();

        //Create a user: thebestuser
        String newUserId1 = UUID.randomUUID().toString();
        User newUser = (User)userManager.makeNew();
        newUser.setUuid(newUserId1);
        newUser.setUsername("thebestuser");
        userManager.persist(context, newUser);

        //Test that the user's local sequence number got created and set.
        User theUser = (User)userManager.findByPrimaryKey(context, newUserId1);
        long seqNumber = theUser.getLocalSequence();
        long newUserId1DateCreated = theUser.getDateCreated();
        Assert.assertNotNull(seqNumber);

        //Update this user and test the seq(local sequence should get a +1)
        theUser.setNotes("Update01");
        userManager.persist(context, theUser);
        //You need to get the user object again from the manager
        User updatedUser = (User) userManager.findByPrimaryKey(context, newUserId1);
        long updatedSeqNumber = updatedUser.getLocalSequence();
        Assert.assertEquals(updatedSeqNumber, seqNumber + 1);

        //Lets create another user for syncing purposes
        User anotherUser = (User)userManager.makeNew();
        String newUserId2 = UUID.randomUUID().toString();
        anotherUser.setUuid(newUserId2);
        anotherUser.setUsername("anotheruser");
        userManager.persist(context, anotherUser);

        //Get all entities since sequence number 0 and test their size
        long sequenceNumber = 0;
        List allUsersSince = userManager.getAllSinceSequenceNumber(
                testingUser, context, testingNode.getHost(), sequenceNumber);
        Assert.assertNotNull(allUsersSince);
        //Supposed to be an extra 2 from above
        //Assert.assertEquals(allUsersSince.size(), initialUserCount + 2);
        Assert.assertEquals(allUsersSince.size(), 1); //Should be 1 because user specific


        //Test changeseq manager
        long gottenNextSeqNum = changeSeqManager.getNextChangeByTableName(tableName, context);
        Assert.assertNotNull(gottenNextSeqNum);
        //We cannot test exactly what the value was. Without deleting everything
        // and starting fresh. If we want we can empty the tables before the
        // tests run.
        //Test that we can Allocate +2
        changeSeqManager.getNextChangeAddSeqByTableName(tableName, 2, context);
        long postIncrementGottenNextSeqNumber =
                changeSeqManager.getNextChangeByTableName(tableName, context);
        Assert.assertEquals(postIncrementGottenNextSeqNumber, gottenNextSeqNum + 2);
        //endofpasting


        //Start Sync
        UMSyncResult result =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(result);


        //Get all users and check first:
        List<User> allUsersBeforeIncomingSync = userManager.getAll(context);

        /* Test starting Sync again to check if more to be sent ..
         * There should not be any more  */
        UMSyncResult syncAgainResult =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(syncAgainResult);


        //TODO: Check the entities on endpoint side.



        httpd.stop();

    }
}
