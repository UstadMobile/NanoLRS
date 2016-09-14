/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;

/**
 *
 * @author mike
 */
public interface XapiStatementManager {
    
    public void findByUuid(Object dbContext, int requestId, PersistenceReceiver receiver, String uuid);

    public XapiStatementProxy findByUuidSync(Object dbContext, String uuid);

    public void create(Object dbContext, int requestId, PersistenceReceiver receiver);

    public XapiStatementProxy createSync(Object dbContext);

    public void persistSync(Object dbContext, XapiStatementProxy stmt);

}
