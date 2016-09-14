package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementProxy;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by mike on 9/13/16.
 */
public class XapiForwardingStatementManagerOrmLite implements XapiForwardingStatementManager {

    private PersistenceManagerORMLite persistenceManager;

    public XapiForwardingStatementManagerOrmLite(PersistenceManagerORMLite persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public XapiForwardingStatementProxy createSync(Object dbContext, String uuid) {
        XapiForwardingStatementEntity entity = null;
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            entity = new XapiForwardingStatementEntity();
            entity.setUuid(uuid);
            dao.create(entity);
        }catch(SQLException e) {

        }

        return entity;
    }

    @Override
    public void persistSync(Object dbContext, XapiForwardingStatementProxy forwardingStatement) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            dao.createOrUpdate((XapiForwardingStatementEntity) forwardingStatement);
        }catch(SQLException e) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception saving");
        }
    }

    @Override
    public XapiForwardingStatementProxy findByUuidSync(Object dbContext, String uuid) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            return dao.queryForId(uuid);
        }catch(SQLException e) {

        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XapiForwardingStatementProxy> getAllUnsentStatementsSync(Object dbContext) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            QueryBuilder<XapiForwardingStatementEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().le(XapiForwardingStatementEntity.FIELD_NAME_STATUS, 2);
            return (List<XapiForwardingStatementProxy>)(Object)dao.query(queryBuilder.prepare());
        }catch(SQLException e) {

        }

        return null;
    }
}
