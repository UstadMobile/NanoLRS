package com.ustadmobile.nanolrs.android.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.ustadmobile.nanolrs.android.BuildConfig;
import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEvent;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingListener;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

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
public class TestiXapiForwardingStatement implements XapiStatementsForwardingListener {

    private boolean receivedUpdate = false;

    private XapiStatement watchedStmt;

    private boolean watchedStmtQueueEvtReceived = false;

    private boolean watchedStmtSentEvtReceived = false;

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
        this.watchedStmt = PersistenceManager.getInstance().getStatementManager().findByUuidSync(context, generatedUUID);

        String username = BuildConfig.TESTUSER;
        String password = BuildConfig.TESTPASSWORD;
        String endpointURL = BuildConfig.TESTLRSENDPOINT;

        int countUnsentBefore = manager.getUnsentStatementCount(context);
        XapiStatementsForwardingEndpoint.queueStatement(context, this.watchedStmt,
                endpointURL, username, password);
        Assert.assertEquals("Unsent count increases by one after queueing stmt",
                countUnsentBefore +1, manager.getUnsentStatementCount(context));
        Assert.assertEquals("Queued but not sent status is queued",
                XapiForwardingStatement.STATUS_QUEUED,
                manager.findStatusByXapiStatement(context, watchedStmt));

        Assert.assertTrue("Received event for adding statement to queue", this.receivedUpdate);
        Assert.assertTrue("Statement queued event received with statement", watchedStmtQueueEvtReceived);
        this.receivedUpdate = false;

        XapiForwardingStatement forwardingStmt = manager.findByUuidSync(context, generatedUUID);
        Assert.assertNotNull(forwardingStmt);

        List<XapiForwardingStatement> sendQueue = manager.getAllUnsentStatementsSync(context);
        Assert.assertTrue(sendQueue.size() > 0);

        XapiStatementsForwardingEndpoint.sendQueue(context);
        Assert.assertTrue("Received event for watched statement being sent", watchedStmtSentEvtReceived);
        Assert.assertTrue("Received event after queue sent", this.receivedUpdate);
        Assert.assertEquals("Queued but not sent status is queued",
                XapiForwardingStatement.STATUS_SENT,
                manager.findStatusByXapiStatement(context, watchedStmt));


        XapiStatementsForwardingEndpoint.removeQueueStatusListener(this);

        //Send queue should be empty now
        int numUnsentStatements =manager.getAllUnsentStatementsSync(context).size();
        Assert.assertTrue(numUnsentStatements == 0);
    }

    @Override
    public void queueStatementSent(XapiStatementsForwardingEvent event) {
        if(event.getStatement() != null && event.getStatement().getId().equals(watchedStmt.getId())) {
            watchedStmtSentEvtReceived = true;
        }
    }

    @Override
    public void statementQueued(XapiStatementsForwardingEvent event) {
        if(event.getStatement() != null && event.getStatement().getId().equals(watchedStmt.getId())) {
            watchedStmtQueueEvtReceived = true;
        }
    }

    @Override
    public void queueStatusUpdated(XapiStatementsForwardingEvent event) {
        this.receivedUpdate = true;
    }
}
