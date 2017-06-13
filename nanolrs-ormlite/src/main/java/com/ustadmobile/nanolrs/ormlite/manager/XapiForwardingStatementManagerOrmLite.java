package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiForwardingStatementEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by mike on 9/13/16.
 */
public class XapiForwardingStatementManagerOrmLite extends BaseManagerOrmLite implements XapiForwardingStatementManager {

    public XapiForwardingStatementManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return XapiForwardingStatementEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user) {
        return null;
    }

    @Override
    public XapiForwardingStatement createSync(Object dbContext, String uuid) {
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
    public void persistSync(Object dbContext, XapiForwardingStatement forwardingStatement) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            dao.createOrUpdate((XapiForwardingStatementEntity) forwardingStatement);
        }catch(SQLException e) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception saving");
        }
    }

    @Override
    public XapiForwardingStatement findByUuidSync(Object dbContext, String uuid) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            return dao.queryForId(uuid);
        }catch(SQLException e) {

        }

        return null;
    }

    private QueryBuilder<XapiForwardingStatementEntity, String> getUnsentStatementsQueryBuilder(Object dbContext, Dao<XapiForwardingStatementEntity, String> dao) throws SQLException {
        QueryBuilder<XapiForwardingStatementEntity, String> queryBuilder = dao.queryBuilder();
        queryBuilder.where().lt(XapiForwardingStatementEntity.COLNAME_STATUS, XapiForwardingStatement.STATUS_SENT);
        return queryBuilder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XapiForwardingStatement> getAllUnsentStatementsSync(Object dbContext) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            QueryBuilder<XapiForwardingStatementEntity, String> queryBuilder = getUnsentStatementsQueryBuilder(dbContext, dao);
            return (List<XapiForwardingStatement>)(Object)dao.query(queryBuilder.prepare());
        }catch(SQLException e) {

        }

        return null;
    }

    @Override
    public int getUnsentStatementCount(Object dbContext) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            QueryBuilder<XapiForwardingStatementEntity, String> queryBuilder = getUnsentStatementsQueryBuilder(dbContext, dao);
            queryBuilder.setCountOf(true);
            return (int)dao.countOf(queryBuilder.prepare());
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public int findStatusByXapiStatement(Object dbContext, XapiStatement statement) {
        try {
            Dao<XapiForwardingStatementEntity, String> dao = persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);
            QueryBuilder<XapiForwardingStatementEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(XapiForwardingStatementEntity.COLNAME_STATEMENT, statement.getUuid());
            XapiForwardingStatementEntity entity = dao.queryForFirst(queryBuilder.prepare());
            if(entity != null) {
                return entity.getStatus();
            }else {
                return -2;
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /*
    @Override
    public NanoLrsModel makeNew() throws SQLException {
        return null;
    }
    */
}
