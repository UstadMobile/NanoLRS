package com.ustadmobile.nanolrs.test.core.model;

import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.core.util.LrsIoUtils;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;

import org.json.JSONObject;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by mike on 2/7/17.
 */

public abstract class TestXapiStatement {

    protected XapiStatement entity;

    protected CountDownLatch lock;

    @Test
    public void testCreation() throws Exception {
        Object context = NanoLrsPlatformTestUtil.getContext();


        lock = new CountDownLatch(1);
        PersistenceManager.getInstance().getManager(XapiStatementManager.class).create(context, 1, new PersistenceReceiver() {
            @Override
            public void onPersistenceSuccess(Object result, int requestId) {
                entity = (XapiStatement)result;
                lock.countDown();
            }

            @Override
            public void onPersistenceFailure(Object result, int requestId) {
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull("Entity Created", entity);
        String createdUuid = entity.getUuid();
        entity = null;
        lock = new CountDownLatch(1);

        PersistenceManager.getInstance().getManager(XapiStatementManager.class).findByUuid(context, 2, new PersistenceReceiver() {
            @Override
            public void onPersistenceSuccess(Object result, int requestId) {
                entity = (XapiStatement)result;
                lock.countDown();
            }

            @Override
            public void onPersistenceFailure(Object result, int requestId) {
                lock.countDown();
            }
        }, createdUuid);

        lock.await(2000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull("Entity created found", entity);



        InputStream stmtIn = getClass().getResourceAsStream("/com/ustadmobile/nanolrs/core/xapi-statement-page-experienced.json");
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

        Assert.assertTrue("Found statement made using query", foundLastStmt);
    }
}
