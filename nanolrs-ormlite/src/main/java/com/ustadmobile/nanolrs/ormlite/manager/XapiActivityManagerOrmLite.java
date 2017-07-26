package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 10/2/16.
 */

public class XapiActivityManagerOrmLite extends BaseManagerOrmLiteSyncable implements XapiActivityManager {

    public XapiActivityManagerOrmLite() {

    }

    @Override
    public Class getEntityImplementationClasss() {
        return XapiActivityEntity.class;
    }

    @Override
    public XapiActivity findByActivityId(Object dbContext, String activityId) {
        XapiActivity result = null;
        try {
            Dao<XapiActivityEntity, String> dao = persistenceManager.getDao(XapiActivityEntity.class, dbContext);
            result = dao.queryForId(activityId);
        }catch(SQLException e) {
            System.err.println("Exception findorcreatebyid");
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public XapiActivity makeNew(Object dbContext) {
        XapiActivityEntity entity = new XapiActivityEntity();
        return entity;
    }

    @Override
    public List<NanoLrsModelSyncable> findAllRelatedToUser(Object dbContext, User user)
            throws SQLException{

        Dao<XapiActivityEntity, String> xapiActivityDao =
                persistenceManager.getDao(XapiActivityEntity.class, dbContext);
        QueryBuilder<XapiActivityEntity, String> xapiActivityQB = xapiActivityDao.queryBuilder();
        Where xapiActivityWhere = xapiActivityQB.where();

        Dao<XapiStatementEntity, String> xapistatementDao =
                persistenceManager.getDao(XapiStatementEntity.class, dbContext);
        QueryBuilder<XapiStatementEntity, String> xapiStatementQB = xapistatementDao.queryBuilder();
        QueryBuilder<XapiStatementEntity, String> xapiStatementSelect =
                xapiStatementQB.selectColumns(XapiStatementEntity.COLNAME_ACTIVITY);
        Where xapiStatementWhere = xapiStatementSelect.where();
        xapiStatementWhere.eq(XapiStatementEntity.COLNAME_AGENT, user).or().eq(XapiStatementEntity.COLNAME_ACTOR, user);
        //xapiStatementWhere.and().gt(XapiStatementEntity.COLNAME_LOCAL_SEQUENCE)


        XapiStatementManagerOrmLite xapiStatementManager =
                PersistenceManagerORMLite.getInstance().getManager(XapiStatementManagerOrmLite.class);
        PreparedQuery xapiStatementPQ = xapiStatementManager.findAllRelatedToUserQuery(dbContext, user);



        return null;
    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) throws SQLException {

        XapiAgentManager agentManager =
                PersistenceManager.getInstance().getManager(XapiAgentManager.class);

        //XapiActivity's dao, qb and where:
        Dao<XapiActivityEntity, String> xapiActivityDao =
                persistenceManager.getDao(XapiActivityEntity.class, dbContext);
        QueryBuilder<XapiActivityEntity, String> xapiActivityQB = xapiActivityDao.queryBuilder();
        Where xapiActivityWhere = xapiActivityQB.where();


        //getting agent from user
        List<XapiAgent> usersCorrespondingAgents = agentManager.findByUser(dbContext, user);
        XapiAgent userCorrespondingAgent;
        if(usersCorrespondingAgents != null &&
                !usersCorrespondingAgents.isEmpty() && usersCorrespondingAgents.size() == 1){
            userCorrespondingAgent = usersCorrespondingAgents.get(0);
        }else{
            return null;
        }



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

        //also add authority

        List res = xapiStatementDao.query(xapiStatementPQ);



        return xapiStatementPQ;
    }

    @Override
    public void createOrUpdate(Object dbContext, XapiActivity data) {
        try {
            super.persist(dbContext, data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*
        try {
            Dao<XapiActivityEntity, String> dao = persistenceManager.getDao(XapiActivityEntity.class, dbContext);
            dao.createOrUpdate((XapiActivityEntity)data);
        }catch(SQLException e) {
            System.err.println("Exception createOrUpdate");
            e.printStackTrace();
        }
        */
    }

    @Override
    public void deleteByActivityId(Object dbContext, String activityId) {
        try {
            Dao<XapiActivityEntity, String> dao = persistenceManager.getDao(XapiActivityEntity.class, dbContext);
            dao.deleteById(activityId);
        }catch(SQLException e) {
            System.err.println("Exception deleteById");
            e.printStackTrace();
        }
    }

    @Override
    public List<XapiActivity> findByAuthority(Object dbContext, XapiAgent agent)
            throws SQLException {
        Dao<XapiActivity, String> dao =
                persistenceManager.getDao(XapiActivity.class, dbContext);
        QueryBuilder<XapiActivity, String> qb = dao.queryBuilder();
        Where where = qb.where();
        where.eq(XapiActivityEntity.COLNAME_AUTHORITY, agent.getUuid());
        List<XapiActivity> activities = dao.query(qb.prepare());
        if(activities != null && !activities.isEmpty()){
            return activities;
        }else{
            return null;
        }
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException {
        super.persist(dbContext, data);
    }

}
