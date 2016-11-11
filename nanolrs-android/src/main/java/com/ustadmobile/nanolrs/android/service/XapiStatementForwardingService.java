package com.ustadmobile.nanolrs.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ustadmobile.nanolrs.android.persistence.PersistenceManagerAndroid;
import com.ustadmobile.nanolrs.core.endpoints.XapiStatementsForwardingEndpoint;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.util.Timer;
import java.util.TimerTask;

public class XapiStatementForwardingService extends Service {

    private XapiStatementForwardingBinder mBinder = new XapiStatementForwardingBinder();

    private Timer mTimer;

    //Frequency of forwarding statements
    public static int FORWARD_INTERVAL = 60000;

    public XapiStatementForwardingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new XapiStatementForwardingTimerTask(), FORWARD_INTERVAL, FORWARD_INTERVAL);
    }


    public class XapiStatementForwardingBinder extends Binder {
        public XapiStatementForwardingService getService() {
            return XapiStatementForwardingService.this;
        }
    }

    public class XapiStatementForwardingTimerTask extends TimerTask {
        public void run() {
            int numSent = XapiStatementsForwardingEndpoint.sendQueue(XapiStatementForwardingService.this);
        }
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        mTimer = null;
        PersistenceManagerAndroid.getInstanceAndroid().releaseHelperForContext(this);
    }


}
