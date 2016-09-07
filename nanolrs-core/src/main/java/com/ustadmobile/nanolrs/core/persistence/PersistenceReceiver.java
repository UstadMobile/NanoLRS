/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.nanolrs.core.persistence;

/**
 *
 * @author mike
 */
public interface PersistenceReceiver {
    
    public void onPersistenceSuccess(Object result, int requestId);
    
    public void onPersistenceFailure(Object result, int requestId);
    
}
