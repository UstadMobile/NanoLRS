package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiVerbEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;

/**
 * Created by mike on 17/11/16.
 */

public class XapiVerbManagerOrmLite extends BaseManagerOrmLite implements XapiVerbManager {

    public XapiVerbManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return XapiVerbEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user) {
        return null;
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
            Dao<XapiVerbEntity, String> dao = persistenceManager.getDao(XapiVerbEntity.class, dbContext);
            dao.createOrUpdate((XapiVerbEntity)data);
        }catch(SQLException e) {
            e.printStackTrace();
        }
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

    /*
    @Override
    public NanoLrsModel makeNew() throws SQLException {
        return null;
    }
    */
}
