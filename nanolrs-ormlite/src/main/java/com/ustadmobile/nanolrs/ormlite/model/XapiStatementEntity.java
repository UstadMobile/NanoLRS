package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
import com.ustadmobile.nanolrs.core.model.XapiAgentProxy;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.model.XapiVerbProxy;

/**
 * Created by mike on 9/6/16.
 */
@DatabaseTable(tableName="xapi_statements")
public class XapiStatementEntity implements XapiStatementProxy {

    public static final String COLNAME_ID = "id";

    public static final String COLNAME_ACTIVITY = "activity_id";

    public static final String COLNAME_VERB = "verb_id";

    public static final String COLNAME_REGISTRATION = "contextRegistration";

    public static final String COLNAME_TIMESTAMP = "timestamp";

    @DatabaseField(id =  true)
    private String id;

    @DatabaseField(foreign =  true)
    private XapiAgentEntity actor;

    @DatabaseField(foreign = true)
    private XapiVerbEntity verb;

    @DatabaseField(foreign =  true)
    private XapiAgentEntity agent;

    @DatabaseField(foreign = true)
    private XapiActivityEntity activity;

    @DatabaseField
    private String statementRef;

    @DatabaseField
    private boolean resultSuccess;

    @DatabaseField
    private boolean resultComplete;

    @DatabaseField
    private String resultResponse;

    @DatabaseField
    private String resultDuration;

    @DatabaseField
    private float resultScoreScaled;

    @DatabaseField
    private float resultScoreRaw;

    @DatabaseField
    private float resultScoreMin;

    @DatabaseField
    private float resultScoreMax;

    @DatabaseField
    private String resultExtensions;

    @DatabaseField
    private long stored;

    @DatabaseField(foreign = true)
    private XapiAgentEntity authority;

    @DatabaseField(index = true)
    private String contextRegistration;

    @DatabaseField
    private String version;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String fullStatement;

    //Index = true added for Database Version 12
    @DatabaseField(index = true)
    private long timestamp;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getContextRegistration() {
        return contextRegistration;
    }

    public void setContextRegistration(String contextRegistration) {
        this.contextRegistration = contextRegistration;
    }

    @Override
    public XapiAgentProxy getAgent() {
        return agent;
    }

    @Override
    public void setAgent(XapiAgentProxy agent) {
        this.agent = (XapiAgentEntity) agent;
    }

    @Override
    public XapiAgentProxy getActor() {
        return actor;
    }

    @Override
    public void setActor(XapiAgentProxy actor) {
        this.actor = (XapiAgentEntity)actor;
    }

    @Override
    public XapiVerbProxy getVerb() {
        return verb;
    }

    @Override
    public void setVerb(XapiVerbProxy verb) {
        this.verb = (XapiVerbEntity)verb;
    }

    public void setAuthority(XapiAgentEntity authority) {
        this.authority = authority;
    }

    @Override
    public XapiActivityProxy getActivity() {
        return activity;
    }

    @Override
    public void setActivity(XapiActivityProxy activity) {
        this.activity = (XapiActivityEntity)activity;
    }

    @Override
    public String getStatementRef() {
        return statementRef;
    }

    @Override
    public void setStatementRef(String statementRef) {
        this.statementRef = statementRef;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAgent(XapiAgentEntity agent) {
        this.agent = agent;
    }

    public void setActivity(XapiActivityEntity activity) {
        this.activity = activity;
    }

    @Override
    public boolean isResultSuccess() {
        return resultSuccess;
    }

    @Override
    public void setResultSuccess(boolean resultSuccess) {
        this.resultSuccess = resultSuccess;
    }

    @Override
    public boolean isResultComplete() {
        return resultComplete;
    }

    @Override
    public void setResultComplete(boolean resultComplete) {
        this.resultComplete = resultComplete;
    }

    @Override
    public String getResultResponse() {
        return resultResponse;
    }

    @Override
    public void setResultResponse(String resultResponse) {
        this.resultResponse = resultResponse;
    }

    @Override
    public String getResultDuration() {
        return resultDuration;
    }

    @Override
    public void setResultDuration(String resultDuration) {
        this.resultDuration = resultDuration;
    }

    @Override
    public float getResultScoreScaled() {
        return resultScoreScaled;
    }

    @Override
    public void setResultScoreScaled(float resultScoreScaled) {
        this.resultScoreScaled = resultScoreScaled;
    }

    @Override
    public float getResultScoreRaw() {
        return resultScoreRaw;
    }

    @Override
    public void setResultScoreRaw(float resultScoreRaw) {
        this.resultScoreRaw = resultScoreRaw;
    }

    @Override
    public float getResultScoreMin() {
        return resultScoreMin;
    }

    @Override
    public void setResultScoreMin(float resultScoreMin) {
        this.resultScoreMin = resultScoreMin;
    }

    @Override
    public float getResultScoreMax() {
        return resultScoreMax;
    }

    @Override
    public void setResultScoreMax(float resultScoreMax) {
        this.resultScoreMax = resultScoreMax;
    }

    @Override
    public String getResultExtensions() {
        return resultExtensions;
    }

    @Override
    public void setResultExtensions(String resultExtensions) {
        this.resultExtensions = resultExtensions;
    }

    @Override
    public long getStored() {
        return stored;
    }

    @Override
    public void setStored(long stored) {
        this.stored = stored;
    }

    @Override
    public XapiAgentProxy getAuthority() {
        return authority;
    }

    @Override
    public void setAuthority(XapiAgentProxy authority) {
        this.authority = (XapiAgentEntity)authority;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getFullStatement() {
        return fullStatement;
    }

    @Override
    public void setFullStatement(String fullStatement) {
        this.fullStatement = fullStatement;
    }
}
