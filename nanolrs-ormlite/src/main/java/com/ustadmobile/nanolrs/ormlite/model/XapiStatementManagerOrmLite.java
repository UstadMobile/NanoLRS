package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        XapiStatementProxy stmt = findByUuidSync(dbContext, uuid);
        if(stmt != null) {
            receiver.onPersistenceSuccess(stmt, requestId);
        }else {
            receiver.onPersistenceFailure(null, requestId);
        }

    }

    @Override
    public XapiStatementProxy findByUuidSync(Object dbContext, String uuid) {
        XapiStatementEntity result = null;
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            result = dao.queryForId(uuid);
        }catch(SQLException e) {

        }

        return result;
    }

    @Override
    public void create(Object dbContext, int requestId, PersistenceReceiver receiver) {
        XapiStatementProxy entity = createSync(dbContext);
        if(entity != null) {
            receiver.onPersistenceSuccess(entity, requestId);
        }else {
            receiver.onPersistenceFailure("err", requestId);
        }
    }

    @Override
    public XapiStatementProxy createSync(Object dbContext) {
        XapiStatementEntity obj = null;
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            obj = new XapiStatementEntity();
            obj.setId(UUID.randomUUID().toString());
            dao.create(obj);
        }catch(SQLException e) {

        }
        return obj;
    }

    public void persistSync(Object dbContext, XapiStatementProxy stmt) {
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            dao.createOrUpdate((XapiStatementEntity)stmt);
            Logger.getLogger(getClass().getName()).log(Level.INFO, "persisted stmt");
        }catch(SQLException e) {

        }
    }
}
