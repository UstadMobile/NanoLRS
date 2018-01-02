package com.ustadmobile.nanolrs.test.core.model;


import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEvent;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingListener;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;
import com.ustadmobile.nanolrs.buildconfig.TestConstantsCore;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;


import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * Created by mike on 2/7/17.
 */

public class TestXapiForwardingStatement implements XapiStatementsForwardingListener {

    protected boolean receivedUpdate = false;

    protected XapiStatement watchedStmt;

    protected boolean watchedStmtQueueEvtReceived = false;

    protected boolean watchedStmtSentEvtReceived = false;

    @Test
    public void testForwarding() throws Exception {
        /* Disabling test 02/Jan/2018
        Object context = NanoLrsPlatformTestUtil.getContext();
        InputStream stmtIn = getClass().getResourceAsStream("/com/ustadmobile/nanolrs/core/xapi-statement-page-experienced.json");

        JSONObject stmtObj = new JSONObject(LrsIoUtils.inputStreamToString(stmtIn));
        XapiStatementsForwardingEndpoint.addQueueStatusListener(this);
        String generatedUUID = XapiStatementsEndpoint.putStatement(stmtObj, context);
        XapiForwardingStatementManager manager = PersistenceManager.getInstance().getManager(XapiForwardingStatementManager.class);
        this.watchedStmt = PersistenceManager.getInstance().getManager(XapiStatementManager.class).findByUuidSync(context, generatedUUID);

        String username = TestConstantsCore.TESTUSER;
        String password = TestConstantsCore.TESTPASSWORD;
        String endpointURL = TestConstantsCore.TESTLRSENDPOINT;

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
        */
    }

    @Override
    public void queueStatementSent(XapiStatementsForwardingEvent event) {
        if(event.getStatement() != null && event.getStatement().getUuid().equals(watchedStmt.getUuid())) {
            watchedStmtSentEvtReceived = true;
        }
    }

    @Override
    public void statementQueued(XapiStatementsForwardingEvent event) {
        if(event.getStatement() != null && event.getStatement().getUuid().equals(watchedStmt.getUuid())) {
            watchedStmtQueueEvtReceived = true;
        }
    }

    @Override
    public void queueStatusUpdated(XapiStatementsForwardingEvent event) {
        this.receivedUpdate = true;
    }

}
