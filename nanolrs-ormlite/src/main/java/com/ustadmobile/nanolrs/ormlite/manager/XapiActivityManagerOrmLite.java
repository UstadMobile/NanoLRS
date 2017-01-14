package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.ormlite.model.XapiActivityEntity;
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
    public XapiActivity findById(Object dbContext, String id) {
        XapiActivity result = null;
        try {
            Dao<XapiActivityEntity, String> dao = persistenceManager.getDao(XapiActivityEntity.class, dbContext);
            result = dao.queryForId(id);
        }catch(SQLException e) {
            System.err.println("Exception findorcreatebyid");
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public XapiActivity makeNew(Object dbContext) {
        return new XapiActivityEntity();
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
    public void deleteById(Object dbContext, String id) {
        try {
            Dao<XapiActivityEntity, String> dao = persistenceManager.getDao(XapiActivityEntity.class, dbContext);
            dao.deleteById(id);
        }catch(SQLException e) {
            System.err.println("Exception deleteById");
            e.printStackTrace();
        }
    }
}
