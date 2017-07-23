package com.ustadmobile.nanolrs.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by varuna on 7/23/2017.
 */

public class UMSyncService extends Service {


    private UMSyncBinder mBinder = new UMSyncBinder();

    private Object context = null;

    private Timer mTimer;

    //Frequency of forwarding statements ( in ms ?) [ 60k = 1 minute ]
    public static int FORWARD_INTERVAL = 60000;

    public UMSyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class UMSyncBinder extends Binder {
        public UMSyncService getService() {
            return UMSyncService.this;
        }
    }

    @Override
    public void onCreate() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new UMSyncTimerTask(), FORWARD_INTERVAL, FORWARD_INTERVAL);
    }

    public class UMSyncTimerTask extends TimerTask {
        public void run() {
            User loggedInUser = null;
            Node mainNode = null;
            try {
                UMSyncEndpoint.startSync(loggedInUser, mainNode, getApplicationContext());
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        mTimer = null;
    }
}
