package com.ustadmobile.nanolrs.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by varuna on 7/23/2017.
 */

public class UMSyncService extends Service {


    private UMSyncBinder mBinder = new UMSyncBinder();

    private Object context = null;

    private Timer mTimer;

    private User loggedInUser;

    private Node endNode;

    //Frequency of forwarding statements ( in ms ?) [ 60k = 1 minute ]
    public static int FORWARD_INTERVAL = 30000;

    public UMSyncService() {
    }

    public void setLoggedInUser(User user){
        loggedInUser = user;
    }

    public void setEndNode(Node node){
        endNode = node;
    }

    public void setContext(){
        context = getApplicationContext();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class UMSyncBinder extends Binder {
        public UMSyncBinder(){

        }
        public UMSyncService getService() {
            return UMSyncService.this;
        }
    }

    @Override
    public void onCreate() {
        System.out.println("onCreate UMSyncService..");
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new UMSyncTimerTask(), FORWARD_INTERVAL, FORWARD_INTERVAL);
        setContext();
    }

    public class UMSyncTimerTask extends TimerTask {
        public void run() {
            try {
                System.out.println("starting sync UMSyncTimerTask ..");
                UMSyncEndpoint.startSync(loggedInUser, endNode, context);
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
