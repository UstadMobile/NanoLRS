package com.ustadmobile.nanolrs.ormlite.persistence;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiUserManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.model.XapiForwardingStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiUserManagerOrmLite;

import java.sql.SQLException;

/**
 * Created by mike on 9/6/16.
 */
public abstract class PersistenceManagerORMLite extends PersistenceManager {

    public abstract <D extends Dao<T, ?>, T> D getDao(Class<T> clazz, Object dbContext) throws SQLException;

    private XapiStatementManager xapiStatementManager;

    private XapiForwardingStatementManager xapiForwardingStatementManager;

    private XapiUserManager xapiUserManager;



    public PersistenceManagerORMLite() {
        xapiStatementManager = new XapiStatementManagerOrmLite(this);
        xapiForwardingStatementManager = new XapiForwardingStatementManagerOrmLite(this);
        xapiUserManager = new XapiUserManagerOrmLite(this);
    }

    @Override
    public XapiStatementManager getStatementManager() {
        return xapiStatementManager;
    }

    @Override
    public XapiForwardingStatementManager getForwardingStatementManager() {
        return xapiForwardingStatementManager;
    }

    @Override
    public XapiUserManager getUserManager() {
        return xapiUserManager;
    }
}
