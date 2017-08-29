package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiAgentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStateEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 10/2/16.
 */

public class XapiStateManagerOrmLite extends BaseManagerOrmLiteSyncable implements XapiStateManager {

    public XapiStateManagerOrmLite() {
    }

    //From BaseManagerOrmLite:

    @Override
    public Class getEntityImplementationClasss() {
        return XapiStateEntity.class;
    }


    // From XapiStateManager:

    @Override
    public List<NanoLrsModelSyncable> findAllRelatedToUser(Object dbContext, User user)
            throws SQLException {
        //Not Used right now.
        return null;
    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) throws SQLException {

        XapiAgentManager agentManager =
                PersistenceManager.getInstance().getManager(XapiAgentManager.class);

        //getting agent from user
        List<XapiAgent> usersCorrespondingAgents = agentManager.findByUser(dbContext, user);
        XapiAgent userCorrespondingAgent;
        if(usersCorrespondingAgents != null &&
                !usersCorrespondingAgents.isEmpty() && usersCorrespondingAgents.size() == 1){
            userCorrespondingAgent = usersCorrespondingAgents.get(0);
        }else{
            return null;
        }

        //XapiState's dao, qb and where:
        Dao<XapiStateEntity, String> xapiStateDao =
                persistenceManager.getDao(XapiStateEntity.class, dbContext);
        QueryBuilder<XapiStateEntity, String> xapiStateQB = xapiStateDao.queryBuilder();
        QueryBuilder<XapiStateEntity, String> xapiStateSelectQB = xapiStateQB.selectColumns(XapiStateEntity.COLNAME_UUID);
        Where xapiStateWhere = xapiStateSelectQB.where();
        xapiStateWhere.eq(XapiStateEntity.COLNAME_AGENT, userCorrespondingAgent.getUuid());
        PreparedQuery pq = xapiStateSelectQB.prepare();


        //TODODone: maybe we need to do more like get all state from statement's activity?
        //Update: Ignoring, as we get it from
        //XapiStatement's dao, qb and where:
        Dao<XapiStatementEntity, String> xapiStatementDao =
                persistenceManager.getDao(XapiStatementEntity.class, dbContext);
        QueryBuilder<XapiStatementEntity, String> xapiStatementQB = xapiStatementDao.queryBuilder();
        QueryBuilder<XapiStatementEntity, String> xapiStatementSelect =
                xapiStatementQB.selectColumns(XapiStatementEntity.COLNAME_ACTIVITY);
        Where xapiStatementWhere = xapiStatementSelect.where();
        xapiStatementWhere.eq(XapiStatementEntity.COLNAME_AGENT, userCorrespondingAgent.getUuid())
                .or().eq(XapiStatementEntity.COLNAME_ACTOR, userCorrespondingAgent.getUuid());

        PreparedQuery xapiStatementPQ = xapiStatementSelect.prepare();

        List res = xapiStatementDao.query(xapiStatementPQ);



        return pq;
    }

    @Override
    public XapiState makeNew(Object dbContext) {
        return new XapiStateEntity();
    }

    @Override
    public void persist(Object dbContext, XapiState data) {
        try {
            persistenceManager.getDao(XapiStateEntity.class, dbContext).createOrUpdate((XapiStateEntity)data);
        }catch(SQLException e) {
            System.err.println("state manager persist exception");
            e.printStackTrace();
        }
    }

    /*
    @Override
    public XapiStateProxy findByActivityAndAgent(Object dbContext, XapiActivityProxy activity, XapiAgentProxy agent, String registrationUuid, String stateId) {
        try {
            Dao<XapiStateEntity, String> dao = persistenceManager.getDao(XapiStateEntity.class, dbContext);
            QueryBuilder<XapiStateEntity, String> query = dao.queryBuilder();
            Where<XapiStateEntity, String> where = query.where();
            where.eq(XapiStateEntity.COLNAME_ACTIVITY, activity);
            where.eq(XapiStateEntity.COLNAME_AGENT, agent);
            if(registrationUuid != null) {
                where.eq(XapiStateEntity.COLNAME_REGISTRATION, registrationUuid);
            }else {
                where.isNull(XapiStateEntity.COLNAME_REGISTRATION);
            }
            where.eq(XapiStateEntity.COLNAME_STATEID, stateId);
            where.and(4);

            return dao.queryForFirst(query.prepare());
        }catch(SQLException e) {
            System.err.println("Exception in findByActivityAndAgent");
            e.printStackTrace();
        }

        return null;
    }
    */

    @Override
    public XapiState findByActivityAndAgent(Object dbContext, String activityId, String agentMbox, String agentAccountName, String agentAccountHomepage, String registrationUuid, String stateId) {
        try {
            Dao<XapiStateEntity, String> dao = persistenceManager.getDao(XapiStateEntity.class, dbContext);
            QueryBuilder<XapiStateEntity, String> query = dao.queryBuilder();


            Dao<XapiAgentEntity, String> agentDao = persistenceManager.getDao(XapiAgentEntity.class, dbContext);
            QueryBuilder<XapiAgentEntity, String>  agentQuery = XapiAgentManagerOrmLite.makeAgentQuery(agentDao, agentMbox, agentAccountName, agentAccountHomepage);
            query.leftJoin(agentQuery);

            Where<XapiStateEntity, String> where = query.where();
            XapiActivityEntity activity = new XapiActivityEntity();
            activity.setActivityId(activityId);

            where.eq(XapiStateEntity.COLNAME_ACTIVITY, activity);
            where.eq(XapiStateEntity.COLNAME_STATE_ID, stateId);
            if(registrationUuid != null) {
                where.eq(XapiStateEntity.COLNAME_REGISTRATION, registrationUuid);
            }else {
                where.isNull(XapiStateEntity.COLNAME_REGISTRATION);
            }
            where.and(3);

            return dao.queryForFirst(query.prepare());
        }catch(SQLException e) {
            System.err.println("Exception in findByActivityAndAgent");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean delete(Object dbContext, XapiState data) {
        try {
            Dao<XapiStateEntity, String> dao = persistenceManager.getDao(XapiStateEntity.class, dbContext);
            dao.delete((XapiStateEntity)data);
            return true;
        }catch(SQLException e) {
            System.err.println("Exception in XapiStateManagerOrmLite delete");
            e.printStackTrace();
        }

        return false;
    }

    /*
    @Override
    public NanoLrsModel makeNew() throws SQLException {
        return null;
    }
    */
}
