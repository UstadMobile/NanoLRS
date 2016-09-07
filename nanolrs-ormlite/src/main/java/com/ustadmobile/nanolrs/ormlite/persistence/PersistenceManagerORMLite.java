package com.ustadmobile.nanolrs.ormlite.persistence;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.model.XapiStatementManagerOrmLite;

import java.sql.SQLException;

/**
 * Created by mike on 9/6/16.
 */
public abstract class PersistenceManagerORMLite extends PersistenceManager {

    public abstract <D extends Dao<T, ?>, T> D getDao(Class<T> clazz, Object dbContext) throws SQLException;

    private XapiStatementManager xapiStatementManager;

    public PersistenceManagerORMLite() {
        xapiStatementManager = new XapiStatementManagerOrmLite(this);
    }

    @Override
    public XapiStatementManager getStatementManager() {
        return xapiStatementManager;

    }

}
