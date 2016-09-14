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
    String getId();
    
    void setId(String id);

    XapiAgentProxy getAgent();

    void setAgent(XapiAgentProxy agent);

    XapiActivityProxy getActivity();

    void setActivity(XapiActivityProxy activity);

    XapiAgentProxy getActor();

    void setActor(XapiAgentProxy actor);

    XapiVerbProxy getVerb();

    void setVerb(XapiVerbProxy verb);

    //TODO: Check usage of sub statement

    String getStatementRef();

    void setStatementRef(String statementRef);

    boolean isResultSuccess();

    void setResultSuccess(boolean success);

    boolean isResultComplete();

    void setResultComplete(boolean resultComplete);

    String getResultResponse();

    void setResultResponse(String resultResponse);

    String getResultDuration();

    void setResultDuration(String resultDuration);

    float getResultScoreScaled();

    void setResultScoreScaled(float resultScoreScaled);

    float getResultScoreRaw();

    void setResultScoreRaw(float resultScoreRaw);

    float getResultScoreMin();

    void setResultScoreMin(float resultScoreMin);

    float getResultScoreMax();

    void setResultScoreMax(float resultScoreMac);

    String getResultExtensions();

    void setResultExtensions(String resultExtensions);

    long getStored();

    void setStored(long stored);

    long getTimestamp();

    void setTimestamp(long timestamp);

    XapiAgentProxy getAuthority();

    void setAuthority(XapiAgentProxy authority);

    public String getContextRegistration();

    public void setContextRegistration(String contextRegistration);

    //TODO: Handle many to many relations : context_ca_parent, grouping, category, other

    String getVersion();

    void setVersion(String version);

    String getFullStatement();

    void setFullStatement(String fullStatement);


}
