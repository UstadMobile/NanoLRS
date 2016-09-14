package com.ustadmobile.nanolrs.android.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerFactoryAndroid;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsEndpoint;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.ormlite.model.XapiStatementEntity;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by mike on 9/6/16.
 */
@RunWith(AndroidJUnit4.class)
public class TestXapiStatement {

    private XapiStatementEntity entity;

    private CountDownLatch lock;

    @Before
    public void setUp() throws Exception {
        PersistenceManager.setPersistenceManagerFactory(new PersistenceManagerFactoryAndroid());
    }

    @Test
    public void testCreation() throws Exception {
        Context context = InstrumentationRegistry.getContext();
        Assert.assertNotNull(context);

        lock = new CountDownLatch(1);
        PersistenceManager.getInstance().getStatementManager().create(context, 1, new PersistenceReceiver() {
            @Override
            public void onPersistenceSuccess(Object result, int requestId) {
                entity = (XapiStatementEntity)result;
                lock.countDown();
            }

            @Override
            public void onPersistenceFailure(Object result, int requestId) {
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(entity);
        String createdUuid = entity.getId();
        entity = null;
        lock = new CountDownLatch(1);

        PersistenceManager.getInstance().getStatementManager().findByUuid(context, 2, new PersistenceReceiver() {
            @Override
            public void onPersistenceSuccess(Object result, int requestId) {
                entity = (XapiStatementEntity)result;
                lock.countDown();
            }

            @Override
            public void onPersistenceFailure(Object result, int requestId) {
                lock.countDown();
            }
        }, createdUuid);

        lock.await(2000, TimeUnit.MILLISECONDS);

        Assert.assertNotNull(entity);



        InputStream stmtIn = context.getAssets().open("xapi-statement-page-experienced.json");

        long timeStarted = new Date().getTime();
        StringWriter writer = new StringWriter();
        IOUtils.copy(stmtIn, writer, "UTF-8");
        JSONObject stmtObj = new JSONObject(writer.toString());
        String generatedUUID = XapiStatementsEndpoint.putStatement(stmtObj, context);
        Assert.assertNotNull(generatedUUID);

        //now look it up
        XapiStatementProxy retrieved = PersistenceManager.getInstance().getStatementManager().findByUuidSync(context, generatedUUID);
        Assert.assertNotNull(generatedUUID);

        //make sure it has a timestamp
        Assert.assertTrue(retrieved.getTimestamp() >= timeStarted);





    }
}
