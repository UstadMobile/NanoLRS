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
                state.setId(UUID.randomUUID().toString());
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
                //merge the JSON content
                try {
                    JSONObject existingJson = new JSONObject(new String(existingContent, "UTF-8"));
                    JSONObject updatedJson = new JSONObject(new String(stateContent, "UTF-8"));

                    //TODO: This merge as per the XAPI spec should not be recursive
                    JsonUtil.mergeJson(updatedJson, existingJson);
                }catch(IOException e) {
                    System.err.println("Exception in createOrUpdateState JSON Merge");
                    e.printStackTrace();
                }
            }else {
                state.setContent(stateContent);
            }

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


}
