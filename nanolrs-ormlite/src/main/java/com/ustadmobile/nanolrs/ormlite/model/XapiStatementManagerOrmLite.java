package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by mike on 9/6/16.
 */
public class XapiStatementManagerOrmLite implements XapiStatementManager {

    private PersistenceManagerORMLite persistenceManager;

    public XapiStatementManagerOrmLite(PersistenceManagerORMLite persistenceManager) {
        this.persistenceManager = persistenceManager;
    }


    @Override
    public void findByUuid(Object dbContext, int requestId, PersistenceReceiver receiver, String uuid) {
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            receiver.onPersistenceSuccess(dao.queryForId(uuid),requestId);
        }catch(SQLException e) {
            receiver.onPersistenceFailure(e, requestId);
        }
    }

    @Override
    public void create(Object dbContext, int requestId, PersistenceReceiver receiver) {
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            XapiStatementEntity obj = new XapiStatementEntity();
            obj.setUuid(UUID.randomUUID().toString());
            dao.create(obj);
            receiver.onPersistenceSuccess(obj, requestId);
        }catch(SQLException e) {
            receiver.onPersistenceFailure(e, requestId);
        }
    }
}
