package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mike on 9/6/16.
 */
public class XapiStatementManagerOrmLite extends BaseManagerOrmLiteSyncable implements XapiStatementManager {

    public XapiStatementManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return XapiStatementEntity.class;
    }

    @Override
    public NanoLrsModelSyncable findAllRelatedToUser(Object dbContext, User user) {
        return null;
    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) {
        return null;
    }

    @Override
    public void findByUuid(Object dbContext, int requestId, PersistenceReceiver receiver, String uuid) {
        XapiStatement stmt = findByUuidSync(dbContext, uuid);
        if(stmt != null) {
            receiver.onPersistenceSuccess(stmt, requestId);
        }else {
            receiver.onPersistenceFailure(null, requestId);
        }

    }

    @Override
    public XapiStatement findByUuidSync(Object dbContext, String uuid) {
        XapiStatementEntity result = null;
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            result = dao.queryForId(uuid);
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void create(Object dbContext, int requestId, PersistenceReceiver receiver) {
        XapiStatement entity = createSync(dbContext);
        if(entity != null) {
            receiver.onPersistenceSuccess(entity, requestId);
        }else {
            receiver.onPersistenceFailure("err", requestId);
        }
    }

    @Override
    public XapiStatement createSync(Object dbContext) {
        XapiStatementEntity obj = null;
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            obj = new XapiStatementEntity();
            obj.setUuid(UUID.randomUUID().toString());
            dao.create(obj);
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void persistSync(Object dbContext, XapiStatement stmt) {
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            dao.createOrUpdate((XapiStatementEntity)stmt);
            Logger.getLogger(getClass().getName()).log(Level.INFO, "persisted stmt");
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<? extends XapiStatement> findByParams(Object dbContext, String statementid, String voidedStatemendid, XapiAgent agent, String verb, String activity, String registration, boolean relatedActivities, boolean relatedAgents, long since, long until, int limit) {
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            QueryBuilder<XapiStatementEntity, String> queryBuilder = dao.queryBuilder();
            Where<XapiStatementEntity, String> where = queryBuilder.where();

            boolean whereHasClauses = false;
            if(statementid != null){
                where.eq(XapiStatementEntity.COLNAME_UUID, statementid);
                whereHasClauses = true;
            }

            if(agent != null) {
                if(whereHasClauses)
                    where.and();
                where.eq(XapiStatementEntity.COLNAME_AGENT, agent.getUuid());
                whereHasClauses = true;
            }

            if(verb != null) {
                if(whereHasClauses)
                    where.and();
                where.eq(XapiStatementEntity.COLNAME_VERB, verb);
                whereHasClauses = true;
            }

            if(activity != null) {
                if(whereHasClauses)
                    where.and();
                where.eq(XapiStatementEntity.COLNAME_ACTIVITY, activity);
                whereHasClauses = true;
            }

            if(registration != null) {
                if(whereHasClauses)
                    where.and();
                where.eq(XapiStatementEntity.COLNAME_CONTEXT_REGISTRATION, registration);
                whereHasClauses = true;
            }

            if(since >= 0) {
                if(whereHasClauses)
                    where.and();
                where.gt(XapiStatementEntity.COLNAME_TIMESTAMP, since);
                whereHasClauses = true;
            }

            if(until >= 0) {
                if(whereHasClauses)
                    where.and();
                where.le(XapiStatementEntity.COLNAME_TIMESTAMP, until);
                whereHasClauses = true;
            }

            if(limit > 0){
                queryBuilder.limit(Long.valueOf(limit));
            }

            queryBuilder.orderBy(XapiStatementEntity.COLNAME_TIMESTAMP, false);

            return dao.query(queryBuilder.prepare());
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<? extends XapiStatement> findByProgress(Object dbContext, String activityId, XapiAgent agent, String registration, String[] verbIds, int minProgress) {
        try {
            Dao<XapiStatementEntity, String> dao = persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            QueryBuilder<XapiStatementEntity, String> queryBuilder = dao.queryBuilder();
            Where<XapiStatementEntity, String> where = queryBuilder.where();

            where.eq(XapiStatementEntity.COLNAME_AGENT, agent.getUuid());
            where.and().eq(XapiStatementEntity.COLNAME_ACTIVITY, activityId);
            if(registration != null){
                where.and().eq(XapiStatementEntity.COLNAME_CONTEXT_REGISTRATION, registration);
            }

            for(int i = 0; i < verbIds.length; i++) {
                where.or().eq(XapiStatementEntity.COLNAME_VERB, verbIds[i]);
            }
            where.and(verbIds.length);

            if(agent != null) {
                where.and().eq(XapiStatementEntity.COLNAME_AGENT, agent.getUuid());
            }

            where.and().gt(XapiStatementEntity.COLNAME_RESULT_PROGRESS, minProgress);

            queryBuilder.orderBy(XapiStatementEntity.COLNAME_TIMESTAMP, false);
            return dao.query(queryBuilder.prepare());
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
    @Override
    public NanoLrsModel makeNew() throws SQLException {
        return null;
    }
    */
}
