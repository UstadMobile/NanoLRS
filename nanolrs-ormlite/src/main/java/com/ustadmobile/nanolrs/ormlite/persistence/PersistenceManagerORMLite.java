package com.ustadmobile.nanolrs.ormlite.persistence;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.manager.BaseManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiActivityManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiAgentManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiForwardingStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiStateManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiStatementManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiUserManagerOrmLite;
import com.ustadmobile.nanolrs.ormlite.manager.XapiVerbManagerOrmLite;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Implementation for ORMLite. This is used by both the Android and JDBC implementations.
 *
 * The getDao method must be implemented separately on Android and on JDBC implementations as per
 * the design of ORMLite.
 *
 * Created by mike on 9/6/16.
 */
public abstract class PersistenceManagerORMLite extends PersistenceManager {


    protected WeakHashMap<Class,BaseManagerOrmLite> managersCache;

    public static HashMap<Class, Class<? extends BaseManagerOrmLite>> MANAGER_IMPL_MAP;

    static {
        MANAGER_IMPL_MAP = new HashMap<>();
        registerManagerImplementation(XapiActivityManager.class, XapiActivityManagerOrmLite.class);
        registerManagerImplementation(XapiAgentManager.class, XapiAgentManagerOrmLite.class);
        registerManagerImplementation(XapiForwardingStatementManager.class, XapiForwardingStatementManagerOrmLite.class);
        registerManagerImplementation(XapiStateManager.class, XapiStateManagerOrmLite.class);
        registerManagerImplementation(XapiStatementManager.class, XapiStatementManagerOrmLite.class);
        registerManagerImplementation(XapiUserManager.class, XapiUserManagerOrmLite.class);
        registerManagerImplementation(XapiVerbManager.class, XapiVerbManagerOrmLite.class);
    }

    /**
     * Return a Dao for the given entity class.
     *
     * @param clazz The entity class: This must be the ORMLite entity class NOT the core proxy interface
     * @param dbContext The db context object for the platform e.g. Android Context object or connection source on JDBC
     * @param <D>
     * @param <T>
     *
     * @return Data access object
     * @throws SQLException
     */
    public abstract <D extends Dao<T, ?>, T> D getDao(Class<T> clazz, Object dbContext) throws SQLException;



    public PersistenceManagerORMLite() {
        managersCache = new WeakHashMap<>();
    }

    public static void registerManagerImplementation(Class coreManager, Class implementation) {
        MANAGER_IMPL_MAP.put(coreManager, implementation);
    }

    @Override
    public <M extends NanoLrsManager<? extends NanoLrsModel, ?>> M getManager(Class<M> managerType) {
        BaseManagerOrmLite manager= managersCache.get(managerType);
        if(manager == null) {
            try {
                manager = MANAGER_IMPL_MAP.get(managerType).newInstance();
                manager.setPersistenceManager(this);
                managersCache.put(managerType, manager);
            }catch(Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return (M)manager;
    }

}
