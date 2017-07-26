package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiVerbEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 17/11/16.
 */

public class XapiVerbManagerOrmLite extends BaseManagerOrmLiteSyncable implements XapiVerbManager {

    public XapiVerbManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return XapiVerbEntity.class;
    }

    @Override
    public List<NanoLrsModelSyncable> findAllRelatedToUser(Object dbContext, User user) throws SQLException{
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

        //XapiVerb's dao, qb and where:
        Dao<XapiVerbEntity, String> xapiVerbDao =
                persistenceManager.getDao(XapiVerbEntity.class, dbContext);
        QueryBuilder<XapiVerbEntity, String> xapiVerbQB = xapiVerbDao.queryBuilder();
        QueryBuilder<XapiVerbEntity, String> xapiVerbSelectQB =
                xapiVerbQB.selectColumns(XapiVerbEntity.COLNAME_VERB_ID);
        Where xapiVerbWhere = xapiVerbSelectQB.where();


        //XapiStatement's dao, qb and where:
        Dao<XapiStatementEntity, String> xapiStatementDao =
                persistenceManager.getDao(XapiStatementEntity.class, dbContext);
        QueryBuilder<XapiStatementEntity, String> xapiStatementQB = xapiStatementDao.queryBuilder();
        QueryBuilder<XapiStatementEntity, String> xapiStatementSelect =
                xapiStatementQB.selectColumns(XapiStatementEntity.COLNAME_VERB);
        Where xapiStatementWhere = xapiStatementSelect.where();
        xapiStatementWhere.eq(XapiStatementEntity.COLNAME_AGENT, userCorrespondingAgent.getUuid())
                .or().eq(XapiStatementEntity.COLNAME_ACTOR, userCorrespondingAgent.getUuid());

        PreparedQuery xapiStatementPQ = xapiStatementSelect.prepare();

        List res = xapiStatementDao.query(xapiStatementPQ);
        return xapiStatementPQ;
    }

    @Override
    public XapiVerb make(Object dbContext, String verbId) {
        XapiVerb verb = new XapiVerbEntity();
        verb.setVerbId(verbId);
        return verb;
    }

    @Override
    public void persist(Object dbContext, XapiVerb data) {
        try {
            super.persist(dbContext, data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*
        try {
            Dao<XapiVerbEntity, String> dao = persistenceManager.getDao(XapiVerbEntity.class, dbContext);
            dao.createOrUpdate((XapiVerbEntity)data);
        }catch(SQLException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public XapiVerb findById(Object dbContext, String id) {
        try {
            Dao<XapiVerbEntity, String> dao = persistenceManager.getDao(XapiVerbEntity.class, dbContext);
            return dao.queryForId(id);
        }catch(SQLException e){
            e.printStackTrace();
        }

        return null;
    }

}
