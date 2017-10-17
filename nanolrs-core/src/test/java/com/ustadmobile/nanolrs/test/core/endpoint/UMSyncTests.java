package com.ustadmobile.nanolrs.test.core.endpoint;

import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncData;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by varuna on 10/9/2017.
 */

public class UMSyncTests {

    //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
    public Object context;
    public Object endpointContext;

    private static boolean setUpIsDone = false;

    UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
    NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);

    @Before
    public void setUp() throws Exception{
        //making sure setup is only called once in this test instance
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
    }

    @Test
    public void simpleCommonMethods() throws Exception {
        //Test camel-case to snake-case
        String snake_case = UMSyncEndpoint.convertCamelCaseNameToUnderscored("testMe");
        if(snake_case.equals("test_me")){
            Assert.assertTrue(true);
        }else{
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testEntityMethods() throws  Exception{
        //Test entity methods - to get primary key
        String pKey = UMSyncEndpoint.getPrimaryKeyFromClass(User.class);
        Assert.assertEquals(pKey, "username");

        //Test entity method - to get primary key method name
        String pKeyMethod = UMSyncEndpoint.getPrimaryKeyMethodFromClass(User.class);
        Assert.assertEquals(pKeyMethod, "getUsername");

        //Test entity method - to get manager from proxy class
        NanoLrsManagerSyncable manager = UMSyncEndpoint.getManagerFromProxyClass(User.class);
        Assert.assertEquals(manager.getClass().getInterfaces()[0], UserManager.class);

        //Test entity method - to get manager from proxy name
        NanoLrsManagerSyncable manager2 =
                UMSyncEndpoint.getManagerFromProxyName(User.class.getCanonicalName());
        Assert.assertEquals(manager2.getClass().getInterfaces()[0], UserManager.class);

        //Test entity method - to get table name
        String tableName = UMSyncEndpoint.getTableNameFromClass(User.class);
        Assert.assertEquals(tableName, "USER");

        //Test entity method - convert a stream to a string (used in incoming sync)
        String s = "The quick brown fox jumped over the lazy dog.";
        InputStream stream = new ByteArrayInputStream(s.getBytes(UMSyncEndpoint.UTF_ENCODING));
        String s2 = UMSyncEndpoint.convertStreamToString2(stream, UMSyncEndpoint.UTF_ENCODING);
        if(s.equals(s2)){
            Assert.assertTrue(true);
        }else{
            Assert.assertTrue(false);
        }

    }


    @Test
    public void testHttpMethods() throws Exception {

        //Test that UMSyncResult works
        int status = 200;
        Map headers = new HashMap();
        headers.put("X-UM-test", "test");
        UMSyncResult result = UMSyncEndpoint.returnEmptyUMSyncResultWithHeader(status, headers);
        Assert.assertEquals(result.getStatus(), status);
        Assert.assertEquals(result.getResponseData().available(), 0);

    }

    @Test
    public void testUserPreSync() throws SQLException {

        //Get change seq value
        Map<Class, Long> allEntitiesSeqNum = UMSyncEndpoint.getAllEntitiesSeqNum(context);
        long userSeqNum = allEntitiesSeqNum.get(User.class).longValue();

        //Add 1 new user:
        UMSyncTestUtils.addUser("testuser1", "secrettestuse1", context);

        //Test that adding a new user +1 s the change seq number
        Map<Class, Long> allEntitiesSeqNum2 = UMSyncEndpoint.getAllEntitiesSeqNum(context);
        long userSeqNum2 = allEntitiesSeqNum2.get(User.class).longValue();
        Assert.assertEquals(userSeqNum2, userSeqNum + 1);
        List<NanoLrsModel> allUsers = userManager.getAllEntities(context);
        long latestSeqNumFromThisList =
                UMSyncEndpoint.getLatestSeqNumFromEntityArray(allUsers);

        //Add two users.
        UMSyncTestUtils.addUser("testuser2", "secrettestuser2", context);
        UMSyncTestUtils.addUser("testuser3", "secrettestuser3", context);
        allUsers = userManager.getAllEntities(context);

        //Test that the new change seq number accounts for new additions
        long latestSeqNumFromThisList_post =
                UMSyncEndpoint.getLatestSeqNumFromEntityArray(allUsers);

        Assert.assertEquals(latestSeqNumFromThisList + 2, latestSeqNumFromThisList_post );

        //Test auto username generation feature.
        Assert.assertFalse(UMSyncEndpoint.isThisUsernameAvailable("testuser3", context));
        Assert.assertTrue(UMSyncEndpoint.isThisUsernameAvailable("testuser4", context));
        Assert.assertTrue(UMSyncEndpoint.isThisUsernameAvailable(
                UMSyncEndpoint.getNextAvailableUsername("testuser3", context), context));

    }

    @Test
    public void testUserSyncBits() throws Exception {

        //Create sync user and get this node
        User syncUser = UMSyncTestUtils.addUser("syncuser01", "secretsyncuser01", context);
        UMSyncTestUtils.checkAndCreateThisNode(null, context);
        Node thisNode = nodeManager.getThisNode(context);

        //Testing sync header creation - used as part of UMSync
        Map<String, String> syncHeader = UMSyncEndpoint.createSyncHeader(syncUser, thisNode);
        Assert.assertNotNull(syncHeader);
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_NODE_ROLE));
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_NODE_UUID));
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_NODE_URL));
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_NODE_HOST));
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_USER_IS_NEW));
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_USER_PASSWORD));
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_USER_USERNAME));
        Assert.assertNotNull(syncHeader.get(UMSyncEndpoint.HEADER_USER_UUID));

        //Get sync entities before statement insertion
        Map.Entry<UMSyncData, Map<Class, Long>> syncInfoResult =
                UMSyncEndpoint.getSyncInfo(syncUser, thisNode, null, null, context);
        int entitiesSizePre = syncInfoResult.getKey().getEntities().size();
        Map.Entry<JSONObject, Map<Class, Long>> newEntities =
                UMSyncEndpoint.getNewEntriesJSON(syncUser, thisNode, null, null, context);
        int entitiesSizePre2 = newEntities.getKey().getJSONArray("data").length();

        //Insert new statements - Generate 5 random statements
        int statementCount = 5;
        UMSyncTestUtils.generateRandomStatement(syncUser, statementCount, context);

        //Get sync entitis after statement insertion
        syncInfoResult =
                UMSyncEndpoint.getSyncInfo(syncUser, thisNode, null, null, context);
        int entitiesSizePost = syncInfoResult.getKey().getEntities().size();
        newEntities =
                UMSyncEndpoint.getNewEntriesJSON(syncUser, thisNode, null, null, context);
        int entitiesSizePost2 = newEntities.getKey().getJSONArray("data").length();

        //Test entities persisted and ready for sync : Additional 3 for Activity, Verb, Agent
        Assert.assertEquals(entitiesSizePre + statementCount + 3, entitiesSizePost);
        Assert.assertEquals(entitiesSizePre2 + statementCount + 3, entitiesSizePost2);

    }

    //For Sync test: Refer to TestUMSync

}
