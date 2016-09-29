/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.nanolrs.core.persistence;

import com.ustadmobile.nanolrs.core.model.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiUserManager;

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
    
    
    public abstract XapiStatementManager getStatementManager();

    public abstract XapiForwardingStatementManager getForwardingStatementManager();

    public abstract XapiUserManager getUserManager();
    
}
