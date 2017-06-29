package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.model.SyncStatus;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.io.InputStream;
import java.util.Map;

/**
 * This sync endpoint is responsible for syncing databases between servers and other UstadMobile
 * instances. Sync is initiated on client and is communicated between other UstadMobile devices
 * and servers via HTTP request.
 * Created by varuna on 6/27/2017.
 */
public interface UMSyncEndpoint {

    /**
     * Handles incoming sync requests. Essentially an endpoint to process request and
     * update database and handle it
     * @param inputStream
     * @param headers
     * @return
     */
    public UMSyncResult handleIncomingSync(InputStream inputStream, Map headers, Object dbContext);

    /**
     * Handles sync process : gets all entites to be synced from syncstatus seqnum and
     * builds entities list to convert to json array to send in a request to host's
     * syncURL endpoint
     * @param syncURL
     * @param host
     * @return
     */
    public UMSyncResult startSync(String syncURL, String host, Object dbContext);
}
