package com.ustadmobile.nanolrs.core.sync;

import java.io.InputStream;
import java.util.Map;

/**
 * This sync endpoint is responsible for syncing databases between servers and other UstadMobile
 * instances. Sync is initiated on client and is communicated between other UstadMobile devices
 * and servers via HTTP request.
 * Created by varuna on 6/27/2017.
 */
public class UMSyncEndpoint {

    public UMSyncResult handleIncomingSync(InputStream inputStream, Map headers){
        //TODO: this
        return null;
    }

    public UMSyncResult startSync(String syncURL, String host){
        //TODO: this
        return null;
    }
}
