package com.ustadmobile.nanolrs.test.core.model;
/**
 * Created by varuna on 6/29/2017.
 */

import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestSyncComponents {
    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();

        //Get managers
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        ChangeSeqManager changeSeqManager = PersistenceManager.getInstance().getManager(
                ChangeSeqManager.class);
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);

        //Get initial seq number for user table - for debugging purposes
        String tableName = "USER";
        long initialSeqNum =
                changeSeqManager.getNextChangeByTableName(tableName, context) -1;

        //Create a node for testing
        String syncURL = "http://httpbin.org/post";
        Node testingNode = (Node) nodeManager.makeNew();
        testingNode.setUUID(UUID.randomUUID().toString());
        testingNode.setUrl(syncURL);
        testingNode.setHost("testhost");
        testingNode.setName("Testing node");
        testingNode.setRole("tester");
        nodeManager.persist(context, testingNode);

        //Create this node
        Node thisNode = (Node) nodeManager.makeNew();
        String thisNodeUUID = UUID.randomUUID().toString();
        thisNode = nodeManager.createThisDeviceNode(UUID.randomUUID().toString(), "node:"+thisNodeUUID,
                "http://localhost:4242/syncendpoint/", context);

        //Create this testing user: testinguser
        //Use it for Sync purposes. Assign it roles and
        //users for testing user specific syncing.
        String newTestingUserID = UUID.randomUUID().toString();
        User testingUser = (User)userManager.makeNew();
        testingUser.setUuid(newTestingUserID);
        testingUser.setUsername("testinguser");
        testingUser.setPassword("secret");
        userManager.persist(context, testingUser);

        //Get number of users already in system
        int initialUserCount = userManager.getAllEntities(context).size();

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


        //Start Sync
        UMSyncResult result =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(result);

        //InputStream to String and back
        String streamString = "The quick brown fox, jumped over the lazy dog.";
        String encoding = "UTF-8";
        InputStream stream = new ByteArrayInputStream(streamString.getBytes(encoding));
        String streamToString = UMSyncEndpoint.convertStreamToString(stream, encoding);
        Assert.assertEquals(streamString, streamString);


        //Get all users and check first:
        List<User> allUsersBeforeIncomingSync = userManager.getAllEntities(context);

        /* Test starting Sync again to check if more to be sent ..
         * There should not be any more  */
        UMSyncResult syncAgainResult =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(syncAgainResult);

        /* Create JSONs to tests handleIncomingSync.
        Includes two new entities
        One Entity Updated
        One Entity Not updated, but sent anyway
         */
        String newUserId3 = UUID.randomUUID().toString();
        String newUserId4 = UUID.randomUUID().toString();
        int userEntitiesCount = 4;
        long currentTime = System.currentTimeMillis();
        String entitiesAsJSONString =
        "{    \"data\" : " +
            "[" +
             //New Entry
             "{\"localSequence\":5,\"storedDate\":\"" + currentTime + "\",\"dateCreated\":\"" + currentTime + "\",\"masterSequence\":0,\"dateModifiedAtMaster\":0,\"pCls\":\"com.ustadmobile.nanolrs.core.model.User\",\"uuid\":\"" +  newUserId3 + "\",\"username\":\"anotheruser2\"}," +
             //New Entry
             "{\"localSequence\":4,\"storedDate\":\"" + currentTime + "\",\"dateCreated\":\"" + currentTime + "\",\"masterSequence\":0,\"notes\":\"Update01\",\"dateModifiedAtMaster\":0,\"pCls\":\"com.ustadmobile.nanolrs.core.model.User\",\"uuid\":\"" + newUserId4 + "\",\"username\":\"thebestuser2\"}," +
             //Same Entity not updated
             "{\"localSequence\":5,\"storedDate\":" + "\"0\"" + ",\"dateCreated\":" + "\"0\"" +",\"masterSequence\":0,\"dateModifiedAtMaster\":0,\"pCls\":\"com.ustadmobile.nanolrs.core.model.User\",\"uuid\":\"" + newUserId2 + "\",\"username\":\"anotheruser\"}," +
             //Same Entity updated
             "{\"localSequence\":4,\"storedDate\":" +"\"0\"" + ",\"dateCreated\":" + newUserId1DateCreated + ",\"masterSequence\":0,\"notes\":\"Update02\",\"dateModifiedAtMaster\":0,\"pCls\":\"com.ustadmobile.nanolrs.core.model.User\",\"uuid\":\"" + newUserId1 + "\",\"username\":\"thebestuser\"}" +
            "]" +
        ", \"info\" :" + "" +
            " [" +
            "{\"pCls\" : \"com.ustadmobile.nanolrs.core.model.User\",\"tableName\" : \"User\",\"count\" : " + userEntitiesCount + ", \"pk\":\"uuid\"}," +
            "{\"pCls\" : \"com.ustadmobile.nanolrs.core.model.AnotherEntity\",\"tableName\" : \"AnotherEntity\",\"count\" : 0, \"pk\":\"uuid\"}" +
            "]" +
        "}"
        ;
        //create input-stream of json to send to sync and give it to endpoint
        InputStream entitiesAsStream =
                new ByteArrayInputStream(entitiesAsJSONString.getBytes(encoding));
        Map<String, String> headers = null;
        Map<String, String> parameters = null;
        UMSyncResult incomingSyncResult = UMSyncEndpoint.handleIncomingSync(
                entitiesAsStream, testingNode, headers, parameters, context);
        Assert.assertNotNull(incomingSyncResult);
        Assert.assertEquals(incomingSyncResult.getStatus(), 200);

        //Get all users after sync:
        List<User> allUsersAfterIncomingSync = userManager.getAllEntities(context);
        Assert.assertEquals(allUsersAfterIncomingSync.size(),
                allUsersBeforeIncomingSync.size() +2);
        //Assert.assertNotNull(incomingSyncResult);

    }
}
