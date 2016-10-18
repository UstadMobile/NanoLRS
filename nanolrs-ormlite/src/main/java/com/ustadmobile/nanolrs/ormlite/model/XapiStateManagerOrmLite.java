package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.endpoints.XapiActivityEndpoint;
import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
import com.ustadmobile.nanolrs.core.model.XapiAgentProxy;
import com.ustadmobile.nanolrs.core.model.XapiStateManager;
import com.ustadmobile.nanolrs.core.model.XapiStateProxy;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by mike on 10/2/16.
 */

public class XapiStateManagerOrmLite extends BaseManagerOrmLite implements XapiStateManager {

    public XapiStateManagerOrmLite(PersistenceManagerORMLite persistenceManager) {
        super(persistenceManager);
    }

    @Override
    public XapiStateProxy makeNew(Object dbContext) {
        return new XapiStateEntity();
    }

    @Override
    public void persist(Object dbContext, XapiStateProxy data) {
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
    public XapiStateProxy findByActivityAndAgent(Object dbContext, String activityId, String agentMbox, String agentAccountName, String agentAccountHomepage, String registrationUuid, String stateId) {
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
            where.eq(XapiStateEntity.COLNAME_STATEID, stateId);
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
}
