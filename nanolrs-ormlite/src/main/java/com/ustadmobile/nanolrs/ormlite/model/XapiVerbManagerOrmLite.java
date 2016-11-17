package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.XapiVerbProxy;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;

/**
 * Created by mike on 17/11/16.
 */

public class XapiVerbManagerOrmLite extends BaseManagerOrmLite implements XapiVerbManager {

    public XapiVerbManagerOrmLite(PersistenceManagerORMLite persistenceManagerORMLite) {
        super(persistenceManagerORMLite);
    }

    @Override
    public XapiVerbProxy make(Object dbContext, String id) {
        XapiVerbProxy verb = new XapiVerbEntity();
        verb.setId(id);
        return verb;
    }

    @Override
    public void persist(Object dbContext, XapiVerbProxy data) {
        try {
            Dao<XapiVerbEntity, String> dao = persistenceManager.getDao(XapiVerbEntity.class, dbContext);
            dao.createOrUpdate((XapiVerbEntity)data);
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public XapiVerbProxy findById(Object dbContext, String id) {
        try {
            Dao<XapiVerbEntity, String> dao = persistenceManager.getDao(XapiVerbEntity.class, dbContext);
            return dao.queryForId(id);
        }catch(SQLException e){
            e.printStackTrace();
        }

        return null;
    }
}
