package com.ustadmobile.nanolrs.test.core.model;
/**
 * Created by varuna on 7/20/2017.
 */

import com.j256.ormlite.support.ConnectionSource;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.http.NanoLrsHttpd;
import com.ustadmobile.nanolrs.jdbc.persistence.PersistenceManagerJDBC;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class TestSync {


    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();

        //Get the endpoint connectionSource from platform db pool
        Object endpointContext = NanoLrsPlatformTestUtil.getSyncEndpointContext();

        //Create Entities at Endpoint
        PersistenceManagerJDBC persistenceManagerJDBC = new PersistenceManagerJDBC();
        persistenceManagerJDBC.init((ConnectionSource) endpointContext);

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

        //Start Sync
        UMSyncResult result =
                UMSyncEndpoint.startSync(testingUser, testingNode, context);
        Assert.assertNotNull(result);


        httpd.stop();

    }
}
