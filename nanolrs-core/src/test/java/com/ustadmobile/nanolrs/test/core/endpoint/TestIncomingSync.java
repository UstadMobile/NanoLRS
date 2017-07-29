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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TestIncomingSync {

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

        allUsersHere = userManager.getAllEntities(context);
        allUsersthere = userManager.getAllEntities(endpointContext);

        allStatementsHere = statementManager.getAllEntities(context);
        allStatementsThere  = statementManager.getAllEntities(endpointContext);



        String handleThisString = "{\n" +
                "    \"data\": [{\n" +
                "        \"dateCreated\": 1501284886194,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"localSequence\": 7,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"notes\": \"User Created via Registration Page\",\n" +
                "        \"password\": \"hh\",\n" +
                "        \"storedDate\": 1501284886225,\n" +
                "        \"username\": \"varunas3\",\n" +
                "        \"uuid\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.User\"\n" +
                "    }, {\n" +
                "        \"accountHomepage\": \"https:\\/\\/umcloud1.ustadmobile.com\\/umlrs\\/\",\n" +
                "        \"accountName\": \"varunas3\",\n" +
                "        \"dateCreated\": 1501284886311,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"localSequence\": 3,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"storedDate\": 1501284886320,\n" +
                "        \"user\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"uuid\": \"7df83cdd-8cc5-47c1-9b60-d69caec89b87\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.XapiAgent\"\n" +
                "    }, {\n" +
                "        \"dateCreated\": 1501284886271,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"fieldName\": 116,\n" +
                "        \"fieldValue\": \"Varuna.singh@gmail.com \",\n" +
                "        \"localSequence\": 16,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"storedDate\": 1501284886277,\n" +
                "        \"user\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"uuid\": \"454c8b62-af53-4ba1-b4f3-4740af498ddb\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.UserCustomFields\"\n" +
                "    }, {\n" +
                "        \"dateCreated\": 1501284886262,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"fieldName\": 115,\n" +
                "        \"fieldValue\": \"H\",\n" +
                "        \"localSequence\": 15,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"storedDate\": 1501284886267,\n" +
                "        \"user\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"uuid\": \"47d1719e-4e8f-4631-8212-71cf7177e117\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.UserCustomFields\"\n" +
                "    }, {\n" +
                "        \"dateCreated\": 1501284886281,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"fieldName\": 114,\n" +
                "        \"fieldValue\": \"H\",\n" +
                "        \"localSequence\": 17,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"storedDate\": 1501284886287,\n" +
                "        \"user\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"uuid\": \"7e777297-d66f-40f0-85ee-4c8d22fcf785\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.UserCustomFields\"\n" +
                "    }, {\n" +
                "        \"dateCreated\": 1501284886253,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"fieldName\": 117,\n" +
                "        \"fieldValue\": \"M\",\n" +
                "        \"localSequence\": 14,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"storedDate\": 1501284886258,\n" +
                "        \"user\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"uuid\": \"a5b17531-3bc3-4eea-8313-826caa342c7d\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.UserCustomFields\"\n" +
                "    }, {\n" +
                "        \"dateCreated\": 1501284886232,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"fieldName\": 118,\n" +
                "        \"fieldValue\": \"5\",\n" +
                "        \"localSequence\": 13,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"storedDate\": 1501284886247,\n" +
                "        \"user\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"uuid\": \"c1d50ff3-3c64-468b-98f1-a3b1d8db8b2f\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.UserCustomFields\"\n" +
                "    }, {\n" +
                "        \"dateCreated\": 1501284886290,\n" +
                "        \"dateModifiedAtMaster\": 0,\n" +
                "        \"fieldName\": 119,\n" +
                "        \"fieldValue\": \"G\",\n" +
                "        \"localSequence\": 18,\n" +
                "        \"masterSequence\": 0,\n" +
                "        \"storedDate\": 1501284886295,\n" +
                "        \"user\": \"4cc15a2e-f040-4be9-9bcd-34bf7949c376\",\n" +
                "        \"uuid\": \"df9fa627-9f8c-463d-9b38-d0ed8711df08\",\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.UserCustomFields\"\n" +
                "    }],\n" +
                "    \"info\": [{\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.User\",\n" +
                "        \"tableName\": \"user\",\n" +
                "        \"count\": 1,\n" +
                "        \"pk\": \"uuid\"\n" +
                "    }, {\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.XapiStatement\",\n" +
                "        \"tableName\": \"xapi_statement\",\n" +
                "        \"count\": 0,\n" +
                "        \"pk\": \"uuid\"\n" +
                "    }, {\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.XapiActivity\",\n" +
                "        \"tableName\": \"xapi_activity\",\n" +
                "        \"count\": 0,\n" +
                "        \"pk\": \"activityId\"\n" +
                "    }, {\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.XapiAgent\",\n" +
                "        \"tableName\": \"xapi_agent\",\n" +
                "        \"count\": 1,\n" +
                "        \"pk\": \"uuid\"\n" +
                "    }, {\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.XapiState\",\n" +
                "        \"tableName\": \"xapi_state\",\n" +
                "        \"count\": 0,\n" +
                "        \"pk\": \"uuid\"\n" +
                "    }, {\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.XapiVerb\",\n" +
                "        \"tableName\": \"xapi_verb\",\n" +
                "        \"count\": 0,\n" +
                "        \"pk\": \"verbId\"\n" +
                "    }, {\n" +
                "        \"pCls\": \"com.ustadmobile.nanolrs.core.model.UserCustomFields\",\n" +
                "        \"tableName\": \"user_custom_fields\",\n" +
                "        \"count\": 6,\n" +
                "        \"pk\": \"uuid\"\n" +
                "    }]\n" +
                "}";



        String encoding = "UTF-8";
        String contentType="application/json";
        InputStream entitiesAsStream =
                new ByteArrayInputStream(handleThisString.getBytes(encoding));

        Map<String, String> headers = new HashMap<>();
        Map<String, String> parameters = new HashMap<>();

        headers.put(UMSyncEndpoint.REQUEST_CONTENT_TYPE, URLEncoder.encode(contentType, UMSyncEndpoint.UTF_ENCODING));

        headers.put(UMSyncEndpoint.HEADER_USER_UUID, testingUser.getUuid());
        headers.put(UMSyncEndpoint.HEADER_USER_USERNAME, testingUser.getUsername());
        headers.put(UMSyncEndpoint.HEADER_USER_PASSWORD, testingUser.getPassword());
        headers.put(UMSyncEndpoint.HEADER_USER_IS_NEW, "true");
        headers.put(UMSyncEndpoint.HEADER_NODE_UUID, testingNode.getUUID());
        headers.put(UMSyncEndpoint.HEADER_NODE_NAME, testingNode.getName());
        headers.put(UMSyncEndpoint.HEADER_NODE_ROLE, testingNode.getRole());
        headers.put(UMSyncEndpoint.HEADER_NODE_HOST, testingNode.getHost());
        headers.put(UMSyncEndpoint.HEADER_NODE_URL, testingNode.getUrl());

        UMSyncResult incomingSyncResult = UMSyncEndpoint.handleIncomingSync(
                entitiesAsStream, thisNode, headers, parameters, endpointContext);

        Assert.assertNotNull(incomingSyncResult);


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




        httpd.stop();
    }
}
