package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;

/**
 * Created by mike on 10/2/16.
 */

public class XapiActivityManagerOrmLite extends BaseManagerOrmLite implements XapiActivityManager {

    public XapiActivityManagerOrmLite(PersistenceManagerORMLite persistenceManagerORMLite) {
        super(persistenceManagerORMLite);
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
    public void createOrUpdate(Object dbContext, XapiActivity data) {
        try {
            Dao<XapiActivityEntity, String> dao = persistenceManager.getDao(XapiActivityEntity.class, dbContext);
            dao.createOrUpdate((XapiActivityEntity)data);
        }catch(SQLException e) {
            System.err.println("Exception createOrUpdate");
            e.printStackTrace();
        }
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
}