package com.ustadmobile.nanolrs.test.core.endpoint;
/**
 * Created by varuna on 7/20/2017.
 */

import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;
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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class TestUMSync {

    public static Object context;

    public static Object endpointContext;


    @Before
    public void setUp() throws Exception{
        endpointContext = NanoLrsPlatformTestUtil.getSyncEndpointContext();
        context = NanoLrsPlatformTestUtil.getContext();

        //PersistenceManager.getInstance().forceInit(endpointContext);
        //TODO: Check if we need to remove below or keep it :
        //PersistenceManager.getInstance().forceInit(context);
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
        UserCustomFieldsManager ucfManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);
        SyncStatusManager ssManager =
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);

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
                "http://localhost:4242/syncendpoint/", false, false, context);


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


        List allUsersHere = userManager.getAllEntities(context);
        List allUsersthere = userManager.getAllEntities(endpointContext);

        List allStatementsHere = statementManager.getAllEntities(context);
        List allStatementsThere  = statementManager.getAllEntities(endpointContext);

        List ssh = ssManager.getAllEntities(context);
        List sst = ssManager.getAllEntities(endpointContext);

        UMSyncResult oneDirectionResult =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);

        ssh = ssManager.getAllEntities(context);
        sst = ssManager.getAllEntities(endpointContext);

        //Create a user update on endpoint
        User userOnEndpoint = (User)userManager.findByPrimaryKey(
                endpointContext, testingUser.getUuid());
        UserCustomFields newUserCustomFieldOnEndpoint = (UserCustomFields)ucfManager.makeNew();
        newUserCustomFieldOnEndpoint.setUuid(UUID.randomUUID().toString());
        newUserCustomFieldOnEndpoint.setUser(userOnEndpoint);
        newUserCustomFieldOnEndpoint.setFieldName(101);
        newUserCustomFieldOnEndpoint.setFieldValue("The Matrix has you.");
        ucfManager.persist(endpointContext, newUserCustomFieldOnEndpoint);

        UserCustomFields newUserCustomFieldOnEndpoint2 = (UserCustomFields)ucfManager.makeNew();
        newUserCustomFieldOnEndpoint2.setUuid(UUID.randomUUID().toString());
        newUserCustomFieldOnEndpoint2.setUser(userOnEndpoint);
        newUserCustomFieldOnEndpoint2.setFieldName(100);
        newUserCustomFieldOnEndpoint2.setFieldValue("The next room");
        ucfManager.persist(endpointContext, newUserCustomFieldOnEndpoint2);

        //Start Sync
        Thread t  = new Thread(new Runnable() {
            User testingUser;
            Node testingNode;
            Object context;
            Object endpointContext;
            NanoLrsHttpd httpd;
            UserManager userManager =
                    PersistenceManager.getInstance().getManager(UserManager.class);
            XapiStatementManager statementManager =
                    PersistenceManager.getInstance().getManager(XapiStatementManager.class);
            UserCustomFieldsManager ucfManager =
                    PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

            public Runnable startSyncThread(User testingUser, Node testingNode,
                                            Object context, Object endpointContext,
                                            NanoLrsHttpd httpd) {
                // store parameter for later user
                this.testingNode = testingNode;
                this.testingUser = testingUser;
                this.context = context;
                this.endpointContext = endpointContext;
                this.httpd = httpd;
                return this;
            }

            public void run() {
                // code goes here.
                UMSyncResult result =
                        null;
                try {
                    result = UMSyncEndpoint.startSync(testingUser, testingNode, context);
                    //TimeUnit.SECONDS.sleep(10);
                    Thread.sleep(10000);
                    List allUsersHere = userManager.getAllEntities(context);
                    List allUsersthere = userManager.getAllEntities(endpointContext);

                    List allStatementsHere = statementManager.getAllEntities(context);
                    List allStatementsThere  = statementManager.getAllEntities(endpointContext);
                    List allUCFHere = ucfManager.getAllEntities(context);
                    List allUCFThere = ucfManager.getAllEntities(endpointContext);

                    int x = 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.assertNotNull(result);

                //httpd.stop();

            }
        }.startSyncThread(testingUser, testingNode, context, endpointContext, httpd));
        t.start();

        //Test starting Sync again to check if more to be sent ..
        //There should not be any more
        UMSyncResult syncAgainResult =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(syncAgainResult);

        allUsersHere = userManager.getAllEntities(context);
        allUsersthere = userManager.getAllEntities(endpointContext);
        allStatementsHere = statementManager.getAllEntities(context);
        allStatementsThere  = statementManager.getAllEntities(endpointContext);
        List ucfh = ucfManager.getAllEntities(context);
        List ucft = ucfManager.getAllEntities(endpointContext);

        ssh = ssManager.getAllEntities(context);
        sst = ssManager.getAllEntities(endpointContext);

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

        // Testing UserCustomFields
        UserCustomFieldsManager userCustomFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);
        UserCustomFields userCustomFields = (UserCustomFields)userCustomFieldsManager.makeNew();

        String universityName = "Web University";
        String name = "Bob Burger";
        String gender = "M";
        String email = "bob@bobsburgers.com";
        String phoneNumber = "+0123456789";
        String faculty = "A faculty";
        String username = "autocustomreguser";
        String password = "secret";

        Map<Integer, String> map = new HashMap<>();
        map.put(980, universityName);
        map.put(981, name);
        map.put(982, gender);
        map.put(983, email);
        map.put(984, phoneNumber);
        map.put(985, faculty);

        userCustomFieldsManager.createUserCustom(map,testingUser, context);
        List relUCFs = userCustomFieldsManager.findByUser(testingUser,context);

        allUsersHere = userManager.getAllEntities(context);
        allUsersthere = userManager.getAllEntities(endpointContext);

        //Start Sync - should have user custom fields (8 of them for this user):
        //Client should get back 2 created - total of 10 here
        //Endpoint should have those 2 + should also get the 8 in initial sync.
        UMSyncResult resultucf =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(resultucf);

        allUsersHere = userManager.getAllEntities(context);
        allUsersthere = userManager.getAllEntities(endpointContext);

        allStatementsHere = statementManager.getAllEntities(context);
        allStatementsThere  = statementManager.getAllEntities(endpointContext);

        ucfh = ucfManager.getAllEntities(context);
        ucft = ucfManager.getAllEntities(endpointContext);

        ssh = ssManager.getAllEntities(context);
        sst = ssManager.getAllEntities(endpointContext);


        TimeUnit.SECONDS.sleep(10);

        allUsersHere = userManager.getAllEntities(context);
        allUsersthere = userManager.getAllEntities(endpointContext);
        allStatementsHere = statementManager.getAllEntities(context);
        allStatementsThere  = statementManager.getAllEntities(endpointContext);
        List allUCFHere = ucfManager.getAllEntities(context);
        List allUCFThere = ucfManager.getAllEntities(endpointContext);

        //Assert.assertEquals(allStatementsThere.size(), 1);
        //Assert.assertEquals(allUCFThere.size(),allUCFHere.size());
        httpd.stop();
    }
}
