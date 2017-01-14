/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;

import java.util.List;

/**
 *
 * @author mike
 */
public interface XapiStatementManager {
    
    public void findByUuid(Object dbContext, int requestId, PersistenceReceiver receiver, String uuid);

    List<? extends XapiStatement> findByParams(Object dbContext, String statementid, String voidedStatemendid, XapiAgent agent, String verb, String activity, String registration, boolean relatedActivities, boolean relatedAgents, long since, long until, int limit);

    public XapiStatement findByUuidSync(Object dbContext, String uuid);

    public void create(Object dbContext, int requestId, PersistenceReceiver receiver);

    public XapiStatement createSync(Object dbContext);

    public void persistSync(Object dbContext, XapiStatement stmt);


}
