package com.ustadmobile.nanolrs.test.core.endpoint;
/**
 * Created by varuna on 7/20/2017.
 */

import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;

public class TestUMSync {

    public Object context;
    public Object endpointContext;
    private static boolean setUpIsDone = false;
    NanoLrsHttpd httpd;
    String endpointUrl;
    Node endpointNode;
    int numberOfExtraStatements = 5;

    @Before
    public void setUp() throws Exception{
        endpointContext = NanoLrsPlatformTestUtil.getSyncEndpointContext();
        context = NanoLrsPlatformTestUtil.getContext();

        if (!setUpIsDone) {
            try {
                PersistenceManager.getInstance().forceInit(endpointContext);
                PersistenceManager.getInstance().forceInit(context);
            }catch (Exception s){
                System.out.println("Ignoring DB Create Exception in tests");
            }
            setUpIsDone = true;
        }
        setUpIsDone = true;

        //Get the endpoint connectionSource from platform db pool
        if(endpointContext == null) {
            Object endpointContext = NanoLrsPlatformTestUtil.getSyncEndpointContext();
        }

        //Create an endpoint server
        httpd = new NanoLrsHttpd(0, endpointContext);

        //Start the server
        httpd.start();
        httpd.mapSyncEndpoint("/sync");
        int serverPort = httpd.getListeningPort();
        endpointUrl = "http://localhost:" + serverPort + "/sync";

        ///Create endpoint node for testing
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        endpointNode = (Node) nodeManager.makeNew();
        endpointNode.setUUID(UUID.randomUUID().toString());
        endpointNode.setUrl(endpointUrl);
        endpointNode.setHost("testhost");
        endpointNode.setName("Testing node");
        endpointNode.setRole("tester");
        nodeManager.persist(context, endpointNode);

        //Create thisNode
        UMSyncTestUtils.checkAndCreateThisNode("http://localhost:4242/syncendpoint/", context);
        UMSyncTestUtils.checkAndCreateThisNode(null, endpointContext);

    }

    @Test
    public void testSeqNumBehaviour() throws Exception{
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);

        User testingUser = UMSyncTestUtils.addUser("testingUserSeqNum", context);
        ChangeSeqManager changeSeqManager = PersistenceManager.getInstance().getManager(
                ChangeSeqManager.class);
        String tableName = UMSyncEndpoint.getTableNameFromClass(User.class);

        //Get all entities since sequence number 0 and test their size
        long sequenceNumber = 0;
        List allUsersSince = userManager.getAllSinceSequenceNumber(
                testingUser, context, endpointNode.getHost(), sequenceNumber);
        Assert.assertEquals(allUsersSince.size(), 1); //Should be 1 because user specific

        //Test changeSeq manager
        long gottenNextSeqNum = changeSeqManager.getNextChangeByTableName(tableName, context);
        Assert.assertNotNull(gottenNextSeqNum);

        //Test that we can Allocate +2
        changeSeqManager.getNextChangeAddSeqByTableName(tableName, 2, context);
        long postIncrementGottenNextSeqNumber =
                changeSeqManager.getNextChangeByTableName(tableName, context);
        Assert.assertEquals(postIncrementGottenNextSeqNumber, gottenNextSeqNum + 2);

        //Test entity update and seq number behaviour then.
        String newUserUsername = "testingUserSeqNum2";
        UMSyncTestUtils.addUser(newUserUsername, context);

        //Test that the user's local sequence number got created and set.
        User theUser = (User)userManager.findByPrimaryKey(context, newUserUsername);
        long seqNumber = theUser.getLocalSequence();
        Assert.assertNotNull(seqNumber);

        //Update this user and test the seq (local sequence should get a +1)
        theUser.setNotes("Update01");
        userManager.persist(context, theUser);
        User updatedUser = (User) userManager.findByPrimaryKey(context, newUserUsername);
        long updatedSeqNumber = updatedUser.getLocalSequence();
        Assert.assertEquals(updatedSeqNumber, seqNumber + 1);
    }

    @Test
    public void testSync() throws Exception {

        //Managers
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        XapiStatementManager statementManager =
                PersistenceManager.getInstance().getManager(XapiStatementManager.class);
        UserCustomFieldsManager ucfManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

        //Create sync User here
        User syncUser = UMSyncTestUtils.addUser("syncUser", context);

        //Test that client-client sync isn't supported (right now)
        UMSyncResult clientToClientResult = UMSyncEndpoint.startSync(syncUser, endpointNode, context);
        Assert.assertEquals(clientToClientResult.getStatus(), HttpURLConnection.HTTP_NOT_ACCEPTABLE);

        //Update endpoint to master
        UMSyncTestUtils.updateNodeType(true, false, endpointContext);

        //Test that syncUser is not at endpoint
        Assert.assertNull(userManager.findByUsername(endpointContext, syncUser.getUsername()));

        //Start sync with no statement data but user data:
        UMSyncResult oneDirectionResult =
                UMSyncEndpoint.startSync(syncUser, endpointNode, context);
        long entitiesCount = oneDirectionResult.getEntitiesCount();
        Assert.assertEquals(oneDirectionResult.getStatus(), HttpURLConnection.HTTP_OK);
        Assert.assertEquals(entitiesCount, 1);

        //Test that syncUser exists at endpoint
        Assert.assertNotNull(userManager.findByUsername(endpointContext, syncUser.getUsername()));

        //Update user on server
        String updateNote = UMSyncTestUtils.updateThisUserWithRandomNotes(syncUser.getUsername(),
                endpointContext);

        //Start sync - test updateNote came back.
        UMSyncEndpoint.startSync(syncUser, endpointNode, context);
        String syncUserUpdatedNote =
                userManager.findByUsername(context, syncUser.getUsername()).getNotes();
        if(!updateNote.equals(syncUserUpdatedNote)){
            Assert.assertTrue(false);
        }

        //Test no more entities to be sent
        UMSyncResult syncAgainResult =
                UMSyncEndpoint.startSync(syncUser, endpointNode, context);
        Assert.assertEquals(syncAgainResult.getEntitiesCount(), 0);

        //Create statements and fields locally
        UMSyncTestUtils.generateRandomStatement(syncUser, numberOfExtraStatements, context);
        UMSyncTestUtils.updateUserCustomFieldsAtRandom(syncUser, context);

        //Test that statements and fields do not exist at endpoint.
        List allUCFThere = ucfManager.getAllEntities(endpointContext);
        List allStatementsThere = statementManager.getAllEntities(endpointContext);
        Assert.assertEquals(allUCFThere.size(), 0);
        Assert.assertEquals(allStatementsThere.size(), 0);

        //Start Sync - test statements and fields now at endpoint.
        UMSyncEndpoint.startSync(syncUser, endpointNode, context);
        List allUCFHere = ucfManager.getAllEntities(context);
        allUCFThere = ucfManager.getAllEntities(endpointContext);
        allStatementsThere = statementManager.getAllEntities(endpointContext);
        Assert.assertEquals(allUCFThere.size(), allUCFHere.size());
        Assert.assertEquals(allStatementsThere.size(), numberOfExtraStatements);

    }

    @After
    public void endTests(){
        if (httpd != null){
            httpd.stop();
        }
    }
}
