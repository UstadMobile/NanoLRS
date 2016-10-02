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
    public XapiActivityProxy findOrCreateById(Object dbContext, String id) {
        XapiActivityProxy result = null;
        try {
            Dao<XapiActivityEntity, String> dao = persistenceManager.getDao(XapiActivityEntity.class, dbContext);
            result = dao.queryForId(id);
            if(result == null) {
                result = new XapiActivityEntity();
                result.setActivityId(id);
            }
        }catch(SQLException e) {
            System.err.println("Exception findorcreatebyid");
            e.printStackTrace();
        }

        return result;
    }
}
