/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.nanolrs.core.model;

/**
 *
 * @author mike
 */
public interface XapiStatementProxy {
    
    /**
     * Interesting info re. performance of UUID fields and SQLite here:
     * 
     * http://stackoverflow.com/questions/11337324/how-to-efficient-insert-and-fetch-uuid-in-core-data/11337522#11337522
     * 
     * @return 
     */
    public String getUuid();
    
    public void setUuid(String uuid);
    
    public long getTimestamp();
    
    public void setTimestamp(long timestamp);
    
    public String getContextRegistration();
    
    public void setContextRegistration(String contextRegistration);
    
}
