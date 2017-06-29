package com.ustadmobile.nanolrs.ormlite.sync;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.model.SyncStatus;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.nanolrs.ormlite.generated.model.SyncStatusEntity;
import com.ustadmobile.nanolrs.ormlite.manager.SyncStatusManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by varuna on 6/29/2017.
 */

public class UMSyncEndpointOrmLite implements UMSyncEndpoint {
    @Override
    public UMSyncResult handleIncomingSync(InputStream inputStream, Map headers, Object dbContext) {
        //TODO: this
        return null;
    }

    /*
    @Override
    public Class getEntityImplementationClasss() {
        return SyncStatusEntity.class;
    }
     */

    @Override
    public UMSyncResult startSync(String syncURL, String host, Object dbContext) {
        //TODO: this
        /*
        Steps:
        1. We check the syncURL < make sure its a valid url
        2. We check the host and see if we have it stored in SyncStatus table
        3. If we don't have the host, we assume this is first time sync
        4. We get all Syncable entites
        5. Loop over every entity: For this entity get seq number from SyncStatus
        6. Get all entities list for that entity that remain to be synced
        7. Convert those entities list to JSON array
        8. Make a request
        9. Send >>
         */

        UMSyncResult result = null;

        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
        /*Dao thisDao =
                persistenceManager.getDao(SyncStatusEntity.class, dbContext);
        */

        return result;
    }
}
