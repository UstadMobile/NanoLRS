package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
import com.ustadmobile.nanolrs.core.model.XapiAgentProxy;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;
import com.ustadmobile.nanolrs.core.model.XapiVerbProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceReceiver;
import com.ustadmobile.nanolrs.core.util.ParseUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 9/13/16.
 */
public class XapiStatementsEndpoint {

    /*
    public class StatementSaver implements PersistenceReceiver {

        JSONObject stmtObj;

        XapiStatementProxy stmt;

        Object dbContext;

        static final int STEP_INIT = 0;

        public StatementSaver(Object dbContext, JSONObject stmtObj, PersistenceReceiver endReceiver) {
            this.dbContext = dbContext;
            this.stmtObj = stmtObj;
            PersistenceManager.getInstance().getStatementManager().create(dbContext, STEP_INIT, this);
        }




        @Override
        public void onPersistenceSuccess(Object result, int requestId) {
            switch(requestId) {
                case STEP_INIT:
                    stmt = (XapiStatementProxy)result;
                    if(stmtObj.has("uuid")) {
                        stmt.setUuid(stmtObj.getString("uuid"));
                    }else {
                        //stmt.setUuid()
                    }

                    //find the agent

            }
        }

        @Override
        public void onPersistenceFailure(Object result, int requestId) {

        }

    }*/


    public static String putStatement(JSONObject stmt, Object dbContext) {
        try {
            XapiStatementProxy stmtProxy =PersistenceManager.getInstance().getStatementManager().createSync(dbContext);
            if(stmt.has("id")) {
                stmtProxy.setId(stmt.getString("id"));
            }else {
                stmtProxy.setId(UUID.randomUUID().toString());
                stmt.put("id", stmtProxy.getId());
            }

            //check timestamp
            if(stmt.has("timestamp")) {
                Calendar cal = ParseUtil.parse8601Timestamp(stmt.getString("timestamp"));
                stmtProxy.setTimestamp(cal.getTime().getTime());
            }else {
                stmtProxy.setTimestamp(new Date().getTime());
                stmt.put("timestamp", ParseUtil.format8601Timestamp(Calendar.getInstance()));
            }

            //check verb
            XapiVerbProxy verb = XapiVerbEndpoint.createOrUpdate(dbContext, stmt.getJSONObject("verb"));
            stmtProxy.setVerb(verb);

            //Check activity
            XapiActivityProxy activity = XapiActivityEndpoint.createOrUpdate(dbContext, stmt.getJSONObject("object"));
            stmtProxy.setActivity(activity);

            //check registration
            if(stmt.has("context")) {
                String registration = stmt.getJSONObject("context").optString("registration", null);
                if(registration != null) {
                    stmtProxy.setContextRegistration(registration);
                }
            }

            stmtProxy.setFullStatement(stmt.toString());

            PersistenceManager.getInstance().getStatementManager().persistSync(dbContext, stmtProxy);

            return stmtProxy.getId();
        }catch(JSONException e) {
            throw new IllegalArgumentException("Invalid json for putstatement", e);
        }
    }

    /**
     *
     * @param statementid
     * @param voidedStatemendid
     * @param agentJSON
     * @param verb
     * @param activity
     * @param registration
     * @param relatedActivities
     * @param relatedAgents
     * @param since
     * @param until
     * @param limit
     * @return
     */
    public static List<? extends XapiStatementProxy> getStatements(Object dbContext, String statementid, String voidedStatemendid, String agentJSON, String verb, String activity, String registration, boolean relatedActivities, boolean relatedAgents, long since, long until, int limit) {
        try {
            XapiAgentProxy agent = agentJSON != null ? XapiAgentEndpoint.createOrUpdate(dbContext, new JSONObject(agentJSON)) : null;
            XapiStatementManager manager = PersistenceManager.getInstance().getStatementManager();

            return manager.findByParams(dbContext, statementid, voidedStatemendid, agent, verb, activity, registration, relatedActivities, relatedAgents, since, until, limit);
        }catch(JSONException e) {
            throw new IllegalArgumentException("Invalid JSON supplied", e);
        }

    }

    public static List<? extends XapiStatementProxy> getStatements(Object dbContext, String statementid, String voidedStatemendid, String agentJSON, String verb, String activity, String registration, boolean relatedActivities, boolean relatedAgents, String since, String until, int limit) {
        long sinceLong = -1, untilLong = -1;
        if(since != null) {
            sinceLong = ParseUtil.parse8601Timestamp(since).getTimeInMillis();
        }

        if(until != null) {
            untilLong = ParseUtil.parse8601Timestamp(until).getTimeInMillis();
        }

        return getStatements(dbContext, statementid, voidedStatemendid, agentJSON, verb,activity, registration, relatedActivities, relatedAgents, sinceLong, untilLong, limit);
    }


}
