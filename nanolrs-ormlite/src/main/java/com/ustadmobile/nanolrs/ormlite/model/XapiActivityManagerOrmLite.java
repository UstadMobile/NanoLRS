package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
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
    public XapiActivityProxy findById(Object dbContext, String id) {
        XapiActivityProxy result = null;
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
    public XapiActivityProxy makeNew(Object dbContext) {
        return new XapiActivityEntity();
    }

    @Override
    public void createOrUpdate(Object dbContext, XapiActivityProxy data) {
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
