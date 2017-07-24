package com.ustadmobile.nanolrs.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ustadmobile.nanolrs.http.NanoLrsHttpd;

import java.io.IOException;

/**
 * Created by mike on 9/28/16.
 */

public class NanoLrsHttpdService extends Service {

    private NanoLrsHttpdServiceBinder mBinder = new NanoLrsHttpdServiceBinder();

    private NanoLrsHttpd nanoLrshttpd;

    public static final int DEFAULT_PORT = 8003;

    public static final String LOGTAG = "NanoLrsHttpdService";

    @Override
    public void onCreate() {
        nanoLrshttpd = new NanoLrsHttpd(DEFAULT_PORT, this);
        try {
            nanoLrshttpd.start();
            Log.i(LOGTAG, "Started Httpd Service on port: " + DEFAULT_PORT);
        }catch(IOException e) {
            Log.e(LOGTAG, "Error starting server", e);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(LOGTAG, "Stop NanoLRS Httpd");
        nanoLrshttpd.stop();
        nanoLrshttpd = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class NanoLrsHttpdServiceBinder extends Binder {
        public NanoLrsHttpdService getService() {
            return NanoLrsHttpdService.this;
        }
    }

}
