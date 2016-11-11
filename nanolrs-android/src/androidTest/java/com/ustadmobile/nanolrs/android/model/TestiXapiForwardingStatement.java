package com.ustadmobile.nanolrs.android.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.BuildConfig;
import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.endpoints.XapiQueueStatusEvent;
import com.ustadmobile.nanolrs.core.endpoints.XapiQueueStatusListener;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementProxy;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.model.XapiStatementEntity;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by mike on 9/13/16.
 */
public class TestiXapiForwardingStatement implements XapiQueueStatusListener{

    private boolean receivedUpdate = false;


    @Before
    public void setUp() throws Exception {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
    }

    @Test
    public void testForwarding() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        InputStream stmtIn = context.getAssets().open("xapi-statement-page-experienced.json");

        StringWriter writer = new StringWriter();
        IOUtils.copy(stmtIn, writer, "UTF-8");
        JSONObject stmtObj = new JSONObject(writer.toString());
        XapiStatementsForwardingEndpoint.addQueueStatusListener(this);
        String generatedUUID = XapiStatementsEndpoint.putStatement(stmtObj, context);
        XapiForwardingStatementManager manager = PersistenceManager.getInstance().getForwardingStatementManager();
        XapiStatementProxy stmtProxy = PersistenceManager.getInstance().getStatementManager().findByUuidSync(context, generatedUUID);

        String username = BuildConfig.TESTUSER;
        String password = BuildConfig.TESTPASSWORD;
        String endpointURL = BuildConfig.TESTLRSENDPOINT;

        int countUnsentBefore = manager.getUnsentStatementCount(context);
        XapiStatementsForwardingEndpoint.queueStatement(context, stmtProxy,
                endpointURL, username, password);
        Assert.assertEquals("Unsent count increases by one after queueing stmt",
                countUnsentBefore +1, manager.getUnsentStatementCount(context));

        Assert.assertTrue("Received event for adding statement to queue", this.receivedUpdate);
        this.receivedUpdate = false;

        XapiForwardingStatementProxy forwardingStmt = manager.findByUuidSync(context, generatedUUID);
        Assert.assertNotNull(forwardingStmt);

        List<XapiForwardingStatementProxy> sendQueue = manager.getAllUnsentStatementsSync(context);
        Assert.assertTrue(sendQueue.size() > 0);

        XapiStatementsForwardingEndpoint.sendQueue(context);
        Assert.assertTrue("Received event after queue sent", this.receivedUpdate);

        XapiStatementsForwardingEndpoint.removeQueueStatusListener(this);

        //Send queue should be empty now
        int numUnsentStatements =manager.getAllUnsentStatementsSync(context).size();
        Assert.assertTrue(numUnsentStatements == 0);
    }


    @Override
    public void queueStatusUpdated(XapiQueueStatusEvent event) {
        this.receivedUpdate = true;
    }
}
