/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;

import java.io.InputStream;

/**
 *
 * @author mike
 */
public abstract class PersistenceManager {
    
    private static PersistenceManager instance;
    
    private static PersistenceManagerFactory sFactory;
    
    public static void setPersistenceManagerFactory(PersistenceManagerFactory factory) {
        sFactory = factory;
    }
    
    
    public static PersistenceManager getInstance() {
        if(instance == null) {
            instance = sFactory.getPersistenceManager();
        }
        
        return instance;
    }

    /**
     * Open an input stream to an asset included with the
     * @param path
     * @return
     */
    public abstract InputStream openAsset(String path);
    
    
    public abstract XapiStatementManager getStatementManager();

    public abstract XapiForwardingStatementManager getForwardingStatementManager();

    public abstract XapiUserManager getUserManager();

    public abstract XapiStateManager getStateManager();

    public abstract XapiActivityManager getActivityManager();

    public abstract XapiAgentManager getAgentManager();

    public abstract XapiVerbManager getVerbManager();

    
}
