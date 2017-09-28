package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiAgentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiForwardingStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStateEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiVerbEntity;

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

    public static Class[] RELATED_ORM_ENTITIES = new Class[]{
            XapiActivityEntity.class, XapiAgentEntity.class, XapiForwardingStatementEntity.class,
            XapiStateEntity.class, XapiVerbEntity.class //, XapiDocumentEntity.class
    };

    @Override
    public Class getEntityImplementationClasss() {
        return XapiStatementEntity.class;
    }

    //Update: We may not be using this anywhere..
    @Override
    public List<NanoLrsModelSyncable> findAllRelatedToUser(Object dbContext, User user)
            throws SQLException {

        // Return all user entities realted to user.
        //You will only return this user as a prepared Query
        long localSeq = 0;
        Dao<XapiStatementEntity, String> thisDao =
                persistenceManager.getDao(XapiStatementEntity.class, dbContext);
        QueryBuilder<XapiStatementEntity, String> subQueryQB = thisDao.queryBuilder();
        QueryBuilder<XapiStatementEntity, String> subQueryQBColumn = subQueryQB.selectColumns("uuid");
        Where subQueryColumnWhere = subQueryQBColumn.where();

        subQueryColumnWhere.eq(XapiStatementEntity.COLNAME_AGENT, user
            ).or().eq(XapiStatementEntity.COLNAME_ACTOR, user);
        subQueryColumnWhere.and().gt(XapiStatementEntity.COLNAME_LOCAL_SEQUENCE, localSeq);
        PreparedQuery<XapiStatementEntity> subQueryColumnPQ = subQueryQBColumn.prepare();


        ///Daos:
        Dao<XapiActivityEntity, String> xapiActivityDao =
                persistenceManager.getDao(XapiActivityEntity.class, dbContext);

        Dao<XapiAgentEntity, String> xapiAgentDao =
                persistenceManager.getDao(XapiAgentEntity.class, dbContext);

        Dao<XapiForwardingStatementEntity, String> xapiForwardingStatementDao =
                persistenceManager.getDao(XapiForwardingStatementEntity.class, dbContext);

        Dao<XapiStateEntity, String> xapiStateDao =
                persistenceManager.getDao(XapiStateEntity.class, dbContext);

        Dao<XapiVerbEntity, String> xapiVerbDao =
                persistenceManager.getDao(XapiVerbEntity.class, dbContext);

        ///QBs:
        QueryBuilder<XapiActivityEntity, String> xapiActivityQB = xapiActivityDao.queryBuilder();
        QueryBuilder<XapiAgentEntity, String> xapiAgentQB = xapiAgentDao.queryBuilder();
        QueryBuilder<XapiForwardingStatementEntity, String> xapiForwardingStatementQB =
                xapiForwardingStatementDao.queryBuilder();
        QueryBuilder<XapiStateEntity, String> xapiStateQB = xapiStateDao.queryBuilder();
        QueryBuilder<XapiVerbEntity, String> xapiVerbQB = xapiVerbDao.queryBuilder();

        ///Wheres:
        Where xapiActivityWhere = xapiActivityQB.where();
        Where xapiAgentWhere = xapiAgentQB.where();
        Where xapiForwardingStatementWhere = xapiForwardingStatementQB.where();
        Where xapiStateWhere = xapiStateQB.where();
        Where xapiVerbWhere = xapiVerbQB.where();

        //xapiActivityWhere.eq()

        return null;
    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) throws SQLException {
        // Return all user entities realted to user.
        //You will only return this user as a prepared Query
        Dao<XapiStatementEntity, String> thisDao =
                persistenceManager.getDao(XapiStatementEntity.class, dbContext);
        XapiAgentManager agentManager =
                PersistenceManager.getInstance().getManager(XapiAgentManager.class);

        QueryBuilder<XapiStatementEntity, String> subQueryQB = thisDao.queryBuilder();
        QueryBuilder<XapiStatementEntity, String> subQueryQBColumn = subQueryQB.selectColumns("uuid");
        Where subQueryColumnWhere = subQueryQBColumn.where();

        List<XapiAgent> usersCorrespondingAgents = agentManager.findByUser(dbContext, user);
        XapiAgent userCorrespondingAgent;
        if(usersCorrespondingAgents != null &&
                !usersCorrespondingAgents.isEmpty() && usersCorrespondingAgents.size() == 1){
            userCorrespondingAgent = usersCorrespondingAgents.get(0);
        }else{
            return null;
        }

        subQueryColumnWhere.eq(XapiStatementEntity.COLNAME_AGENT, userCorrespondingAgent.getUuid()
            ).or().eq(XapiStatementEntity.COLNAME_ACTOR, userCorrespondingAgent.getUuid());


        PreparedQuery<XapiStatementEntity> subQueryColumnPQ = subQueryQBColumn.prepare();
        return subQueryColumnPQ;
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
            super.persist(dbContext, stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<? extends XapiStatement> findByParams(Object dbContext, String statementid,
                                                  String voidedStatemendid, XapiAgent agent,
                                                  String verb, String activity, String registration,
                                                  boolean relatedActivities, boolean relatedAgents,
                                                  long since, long until, int limit) {
        try {
            Dao<XapiStatementEntity, String> dao =
                    persistenceManager.getDao(XapiStatementEntity.class, dbContext);
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
    public List<? extends XapiStatement> findByProgress(Object dbContext, String[] activityIds,
                                                        XapiAgent agent, String registration,
                                                        String[] verbIds, int minProgress) {
        try {
            Dao<XapiStatementEntity, String> dao =
                    persistenceManager.getDao(XapiStatementEntity.class, dbContext);
            QueryBuilder<XapiStatementEntity, String> queryBuilder = dao.queryBuilder();
            Where<XapiStatementEntity, String> where = queryBuilder.where();

            for(int i = 0; i < verbIds.length; i++) {
                where.eq(XapiStatementEntity.COLNAME_VERB, verbIds[i]);
            }
            where.or(verbIds.length);

            for(int i = 0; i < activityIds.length; i++) {
                where.like(XapiStatementEntity.COLNAME_ACTIVITY, activityIds[i] + "%");
            }
            where.or(activityIds.length);

            where.eq(XapiStatementEntity.COLNAME_AGENT, agent.getUuid());

            if(registration != null) {
                where.eq(XapiStatementEntity.COLNAME_CONTEXT_REGISTRATION, registration);
            }
            where.gt(XapiStatementEntity.COLNAME_RESULT_PROGRESS, minProgress);
            where.and(registration != null ? 5: 4);

            queryBuilder.orderBy(XapiStatementEntity.COLNAME_TIMESTAMP, false);
            return dao.query(queryBuilder.prepare());
        }catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
