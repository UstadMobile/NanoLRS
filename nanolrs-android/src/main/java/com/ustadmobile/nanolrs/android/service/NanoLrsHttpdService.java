package com.ustadmobile.nanolrs.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by mike on 9/28/16.
 */

public class NanoLrsHttpdService extends Service {

    private NanoLrsHttpdServiceBinder mBinder = new NanoLrsHttpdServiceBinder();



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class NanoLrsHttpdServiceBinder extends Binder {
        public NanoLrsHttpdService getService() {
            return NanoLrsHttpdService.this;
        }
    }

}
