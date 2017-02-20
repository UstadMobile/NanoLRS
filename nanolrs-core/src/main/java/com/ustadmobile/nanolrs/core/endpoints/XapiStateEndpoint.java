package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by mike on 10/8/16.
 */

public class XapiStateEndpoint {

    /**
     *
     * @param dbContext
     * @param method
     * @param activityId
     * @param agentJson
     * @param registration
     * @param stateId
     * @param stateContent
     * @return
     */
    public static XapiState createOrUpdateState(Object dbContext, String method, String contentType, String activityId, String agentJson, String registration, String stateId, byte[] stateContent) {
        try {
            JSONObject agentJsonObj = new JSONObject(agentJson);
            XapiAgent agent = XapiAgentEndpoint.createOrUpdate(dbContext, agentJsonObj);
            JSONObject activityObj = new JSONObject();
            activityObj.put("id", activityId);
            XapiStateManager manager = PersistenceManager.getInstance().getStateManager();

            String agentMbox =agentJsonObj.optString("mbox", null);
            String agentAccountName = null;
            String agentAccountHopmepage = null;
            if(agentJsonObj.has("account")) {
                agentAccountName = agentJsonObj.getJSONObject("account").getString("name");
                agentAccountHopmepage = agentJsonObj.getJSONObject("account").getString("homePage");
            }

            XapiState state = manager.findByActivityAndAgent(dbContext, activityId,
                    agentMbox, agentAccountName, agentAccountHopmepage, registration, stateId);

            if(state == null) {
                state = manager.makeNew(dbContext);
                state.setUuid(UUID.randomUUID().toString());
                XapiActivity activity = XapiActivityEndpoint.createOrUpdate(dbContext, activityId);
                state.setActivity(activity);
                state.setAgent(agent);
                state.setStateId(stateId);
                state.setRegistration(registration);
            }

            byte[] existingContent = state.getContent();
            if(contentType != null)
                state.setContentType(contentType);


            if(contentType != null && contentType.equals("application/json") && method.equalsIgnoreCase("post") && existingContent != null) {
                //merge the JSON content if possible
                try {
                    JSONObject existingJson = new JSONObject(new String(existingContent, "UTF-8"));
                    JSONObject updatedJson = new JSONObject(new String(stateContent, "UTF-8"));

                    //TODO: This merge as per the XAPI spec should not be recursive
                    JsonUtil.mergeJson(updatedJson, existingJson);
                    stateContent = existingJson.toString().getBytes("UTF-8");
                }catch(JSONException|IOException e) {
                    System.err.println("Exception in createOrUpdateState JSON Merge");
                    e.printStackTrace();
                }
            }

            state.setContent(stateContent);
            manager.persist(dbContext, state);

            return state;
        }catch(JSONException e){
            throw new IllegalArgumentException("Invalid JSON for createOrUpdateState", e);
        }
    }

    public static XapiState getState(Object dbContext, String activityId, String agentJson, String registration, String stateId) {
        try {
            JSONObject agentObject = new JSONObject(agentJson);
            JSONObject agentAccount = agentObject.optJSONObject("account");

            XapiStateManager manager = PersistenceManager.getInstance().getStateManager();
            XapiState stateProxy = manager.findByActivityAndAgent(dbContext, activityId,
                    agentObject.optString("mbox", null),
                    agentAccount != null ? agentAccount.optString("name", null) : null,
                    agentAccount != null? agentAccount.optString("homePage", null) : null,
                    registration, stateId);

            return stateProxy;
        }catch(JSONException e) {
            throw new IllegalArgumentException("Invalid JSON to getState", e);
        }
    }

    public static boolean delete(Object dbContext, String activityId, String agentJson, String registration, String stateId) {
        XapiStateManager manager = PersistenceManager.getInstance().getStateManager();
        XapiState state = getState(dbContext, activityId, agentJson, registration, stateId);
        if(state != null) {
            return manager.delete(dbContext, state);
        }
        return false;
    }


}
