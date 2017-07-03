/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

/**
 *
 * The PersistenceManager on a given platform is responsible to handle persistence on the underlying
 * platform e.g. using ORMLite, SharkORM etc.
 *
 * Classes using an entity can then do so in a platform independent fashion e.g.
 *
 * PersistenceManager.getInstance().getManager(EntityNameManager.class).doSomething
 *
 * new PersistenceManagerFactoryImpl() will link to the platforms's PersistenceManagerFactoryImpl in platform
 * eg: if run from nanolrs-servlet classpath, it will call nanolrs-servet's PersistenceManagerFactoryImpl.method
 *
 * Make sure in your platform you have PersistenceManagerFactoryImpl and a getPersistenceManager
 * method to return the right persistenceManager for that platform.
 *
 * @author mike
 */
public abstract class PersistenceManager {
    
    private static PersistenceManager instance;
    
    public static PersistenceManager getInstance() {
        if(instance == null) {
            // Get platform's persistence Manager.
            instance = new PersistenceManagerFactoryImpl().getPersistenceManager();
        }
        
        return instance;
    }

    /**
     * Get an implementation of a Manager.
     *
     * @param managerType The Manager class E.g. XapiStatementManager.class
     * @param <M> The Manager class E.g. XapiStatementManager.class
     *
     * @return Implementation of the manager as available, null otherwise
     */
    public abstract <M extends NanoLrsManager<? extends NanoLrsModel, ?>> M getManager(Class<M> managerType);

    
}
