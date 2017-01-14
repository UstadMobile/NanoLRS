package com.ustadmobile.nanolrs.ormlite.persistence;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.manager.XapiActivityManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiAgentManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiForwardingStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiStateManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiUserManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiVerbManagerOrmLite;

import java.sql.SQLException;

/**
 * Created by mike on 9/6/16.
 */
public abstract class PersistenceManagerORMLite extends PersistenceManager {

    public abstract <D extends Dao<T, ?>, T> D getDao(Class<T> clazz, Object dbContext) throws SQLException;

    private XapiStatementManager xapiStatementManager;

    private XapiForwardingStatementManager xapiForwardingStatementManager;

    private XapiUserManager xapiUserManager;

    private XapiActivityManager xapiActivityManager;

    private XapiStateManager xapiStateManager;

    private XapiAgentManager xapiAgentManager;

    private XapiVerbManager xapiVerbManager;


    public PersistenceManagerORMLite() {
        xapiStatementManager = new XapiStatementManagerOrmLite(this);
        xapiForwardingStatementManager = new XapiForwardingStatementManagerOrmLite(this);
        xapiUserManager = new XapiUserManagerOrmLite(this);
        xapiActivityManager = new XapiActivityManagerOrmLite(this);
        xapiStateManager = new XapiStateManagerOrmLite(this);
        xapiAgentManager = new XapiAgentManagerOrmLite(this);
        xapiVerbManager = new XapiVerbManagerOrmLite(this);
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

    @Override
    public XapiActivityManager getActivityManager() {
        return xapiActivityManager;
    }

    @Override
    public XapiStateManager getStateManager() {
        return xapiStateManager;
    }

    @Override
    public XapiAgentManager getAgentManager() {
        return xapiAgentManager;
    }

    @Override
    public XapiVerbManager getVerbManager() {
        return xapiVerbManager;
    }

}
