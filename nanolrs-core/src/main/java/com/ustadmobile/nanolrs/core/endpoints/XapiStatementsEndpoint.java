package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.ParseUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 9/13/16.
 */
public class XapiStatementsEndpoint {

    public static String putStatement(JSONObject stmt, Object dbContext) {
        try {
            XapiStatement stmtProxy =PersistenceManager.getInstance().getManager(XapiStatementManager.class).createSync(dbContext);
            XapiAgent agent = XapiAgentEndpoint.createOrUpdate(dbContext, stmt.getJSONObject("actor"));
            stmtProxy.setAgent(agent);
            if(stmt.has("id")) {
                stmtProxy.setUuid(stmt.getString("id"));
            }else {
                stmtProxy.setUuid(UUID.randomUUID().toString());
                stmt.put("id", stmtProxy.getUuid());
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
            XapiVerb verb = XapiVerbEndpoint.createOrUpdate(dbContext, stmt.getJSONObject("verb"));
            stmtProxy.setVerb(verb);

            //Check activity
            XapiActivity activity = XapiActivityEndpoint.createOrUpdate(dbContext, stmt.getJSONObject("object"));
            stmtProxy.setActivity(activity);

            //check registration
            if(stmt.has("context")) {
                String registration = stmt.getJSONObject("context").optString("registration", null);
                if(registration != null) {
                    stmtProxy.setContextRegistration(registration);
                }
            }

            if(stmt.has("result")) {
                JSONObject result = stmt.getJSONObject("result");
                if(result.has("score")) {
                    JSONObject score = result.getJSONObject("score");
                    if(score.has("scaled")){
                        stmtProxy.setResultScoreScaled((float)score.getDouble("scaled"));
                    }
                    if(score.has("raw")) {
                        stmtProxy.setResultScoreRaw((float)score.getDouble("raw"));
                    }
                    if(score.has("min")) {
                        stmtProxy.setResultScoreMin((float)score.getDouble("min"));
                    }
                    if(score.has("max")) {
                        stmtProxy.setResultScoreMax((float)score.getDouble("max"));
                    }
                }

                if(result.has("completion")) {
                    stmtProxy.setResultComplete(result.getBoolean("completion"));
                }

                if(result.has("success")) {
                    stmtProxy.setResultSuccess(result.getBoolean("success"));
                }

                if(result.has("extensions")) {
                    JSONObject extensions = result.getJSONObject("extensions");
                    if(extensions.has("https://w3id.org/xapi/cmi5/result/extensions/progress")){
                        stmtProxy.setResultProgress(extensions.getInt("https://w3id.org/xapi/cmi5/result/extensions/progress"));
                    }
                }
            }

            stmtProxy.setFullStatement(stmt.toString());

            PersistenceManager.getInstance().getManager(XapiStatementManager.class).persistSync(dbContext, stmtProxy);

            return stmtProxy.getUuid();
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
    public static List<? extends XapiStatement> getStatements(Object dbContext, String statementid, String voidedStatemendid, String agentJSON, String verb, String activity, String registration, boolean relatedActivities, boolean relatedAgents, long since, long until, int limit) {
        try {
            XapiAgent agent = agentJSON != null ? XapiAgentEndpoint.createOrUpdate(dbContext, new JSONObject(agentJSON)) : null;
            XapiStatementManager manager = PersistenceManager.getInstance().getManager(XapiStatementManager.class);

            return manager.findByParams(dbContext, statementid, voidedStatemendid, agent, verb, activity, registration, relatedActivities, relatedAgents, since, until, limit);
        }catch(JSONException e) {
            throw new IllegalArgumentException("Invalid JSON supplied", e);
        }

    }

    public static List<? extends XapiStatement> getStatements(Object dbContext, String statementid, String voidedStatemendid, String agentJSON, String verb, String activity, String registration, boolean relatedActivities, boolean relatedAgents, String since, String until, int limit) {
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
