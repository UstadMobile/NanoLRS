package com.ustadmobile.nanolrs.test.core.endpoint;
/**
 * Created by varuna on 7/20/2017.
 */

import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
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
        //TODO: Check if we need to remove below or keep it :
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
        XapiAgentManager agentManager =
                PersistenceManager.getInstance().getManager(XapiAgentManager.class);
        XapiStatementManager statementManager =
                PersistenceManager.getInstance().getManager(XapiStatementManager.class);
        XapiVerbManager verbManager =
                PersistenceManager.getInstance().getManager(XapiVerbManager.class);
        XapiStateManager stateManager =
                PersistenceManager.getInstance().getManager(XapiStateManager.class);
        XapiActivityManager activityManager =
                PersistenceManager.getInstance().getManager(XapiActivityManager.class);

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
        String thisNodeUUID = UUID.randomUUID().toString();
        thisNode = nodeManager.createThisDeviceNode(UUID.randomUUID().toString(), "node:"+thisNodeUUID,
                "http://localhost:4242/syncendpoint/", context);


        ///Create a node for testing
        Node testingNode = (Node) nodeManager.makeNew();
        testingNode.setUUID(UUID.randomUUID().toString());
        testingNode.setUrl(endpointUrl);
        testingNode.setHost("testhost");
        testingNode.setName("Testing node");
        testingNode.setRole("tester");
        nodeManager.persist(context, testingNode);

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






        //Set up Xapi tables:
        InputStream stmtIn = getClass().getResourceAsStream("/com/ustadmobile/nanolrs/core/xapi-statement-synctest.json");
        Assert.assertNotNull("Can get statement resource input stream", stmtIn);
        long timeStarted = new Date().getTime();
        JSONObject stmtObj = new JSONObject(LrsIoUtils.inputStreamToString(stmtIn));
        String generatedUUID = XapiStatementsEndpoint.putStatement(stmtObj, context);
        Assert.assertNotNull("Statement put and UUID generated", generatedUUID);

        //now look it up
        XapiStatement retrieved = PersistenceManager.getInstance().getManager(XapiStatementManager.class).findByUuidSync(context, generatedUUID);
        Assert.assertNotNull("Statement retrieved by UUID", retrieved);

        //make sure it has a timestamp
        Assert.assertTrue("Statement has timestamp added", retrieved.getTimestamp() >= timeStarted);
        Assert.assertTrue("Statement has result success set true", retrieved.isResultSuccess());
        Assert.assertTrue("Statement has completion set true", retrieved.isResultComplete());
        Assert.assertEquals("Progress is 50", 50, retrieved.getResultProgress());

        //make sure that we can find it using a search by parameters
        long since = 0;
        List<? extends XapiStatement> queryResults = XapiStatementsEndpoint.getStatements(context,
                null, null, null, "http://activitystrea.ms/schema/1.0/host",
                "http://www.ustadmobile.com/activities/attended-class/CLASSID", null, false, false,
                null, null, -1);

        boolean foundLastStmt = false;
        for(int i = 0; i < queryResults.size(); i++) {
            if(queryResults.get(i).getUuid().equals(generatedUUID)) {
                foundLastStmt = true;
                break;
            }
        }

        XapiStatementManager manager =
                PersistenceManager.getInstance().getManager(XapiStatementManager.class);
        XapiAgent agent = PersistenceManager.getInstance().getManager(XapiAgentManager.class).findAgentByParams(
                context, null, "newtestinguser", "http://umcloud1.ustadmobile.com/umlrs").get(0);

        agent.setUser(testingUser);
        agentManager.persist(context, agent);

        //testing:
        List allAgents = agentManager.getAllEntities(context);
        List allActivities = activityManager.getAllEntities(context);
        List allVerbs = verbManager.getAllEntities(context);
        List allStatements = statementManager.getAllEntities(context);
        List allStates = stateManager.getAllEntities(context);

        int x=0;

        //Start Sync
        UMSyncResult result =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(result);


        //Get all users and check first:
        List<User> allUsersBeforeIncomingSync = userManager.getAllEntities(context);

        /* Test starting Sync again to check if more to be sent ..
         * There should not be any more  */
        UMSyncResult syncAgainResult =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(syncAgainResult);


        //TODO: Check the entities on endpoint side.

        //Test same user createion
        //Lets create another user for syncing purposes
        User user5 = (User)userManager.makeNew();
        String user5uuid = UUID.randomUUID().toString();
        user5.setUuid(user5uuid);
        user5.setUsername("varunasingh");
        userManager.persist(context, user5);

        List<User> allUsers = userManager.getAllEntities(context);

        User user6 = (User)userManager.makeNew();
        String user6uuid = UUID.randomUUID().toString();
        user6.setUuid(user6uuid);
        user6.setUsername("varunasingh");
        userManager.persist(context, user6);

        allUsers = userManager.getAllEntities(context);

        Assert.assertNotNull(allUsers);


        httpd.stop();

    }
}
