package com.ustadmobile.nanolrs.ormlite.persistence;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStateManager;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.XapiVerbManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.model.XapiActivityManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiAgentManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiForwardingStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiStateManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiUserManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.model.XapiVerbManagerOrmLite;

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
