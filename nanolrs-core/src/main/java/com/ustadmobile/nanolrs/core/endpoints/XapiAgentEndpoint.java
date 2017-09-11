package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 10/6/16.
 */

public class XapiAgentEndpoint {

    public static XapiAgent createOrUpdate(Object dbContext, JSONObject agentJSON) {
        try {
            String mbox = agentJSON.optString("mbox", null);
            String accountHomepage = null;
            String accountName = null;
            if(agentJSON.has("account")) {
                JSONObject accountObj = agentJSON.getJSONObject("account");
                accountHomepage = accountObj.getString("homePage");
                accountName = accountObj.getString("name");
            }

            return createOrUpdate(dbContext, mbox, accountName, accountHomepage);
        }catch(JSONException e) {
            throw new IllegalArgumentException("Invalid Agent JSON supplied", e);
        }
    }

    public static XapiAgent createOrUpdate(Object dbContext, String mbox, String accountName, String accountHomepage){
        XapiAgentManager manager = PersistenceManager.getInstance().getManager(XapiAgentManager.class);
        //Add user to agent
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        List<XapiAgent> matchingAgents = manager.findAgentByParams(
                dbContext, mbox, accountName, accountHomepage);

        if(matchingAgents != null && matchingAgents.size() > 0) {
            //Map user to agent if not already done.
            XapiAgent mappedAgent = matchingAgents.get(0);
            if(mappedAgent.getUser() == null){
                User agentUser = userManager.findById(dbContext, accountName);
                if(agentUser != null){
                    mappedAgent.setUser(agentUser);
                    manager.createOrUpdate(dbContext, mappedAgent);
                }
            }
            return matchingAgents.get(0);
        }

        //does not exist - needs to be created
        XapiAgent agent = manager.makeNew(dbContext);
        agent.setUuid(UUID.randomUUID().toString());
        agent.setMbox(mbox);
        agent.setAccountHomepage(accountHomepage);
        agent.setAccountName(accountName);

        //TODODone: Check if username gets changed, should we set agent before user ?
        //I think its okay, since the agent's user will get set when its created
        //which is before the sync. The sync will change the user's username
        // not the user assigned. So it should be okay.
        //User's persist should change itself and all its associated entries in db.
        //Update: Sync cannot proceed without valid User and username.
        //If username gets changed, userManager.updateUsername(..) changes agent mapping


        /*
        List<User> usersWithAgentName = userManager.findByUsername(dbContext, accountName);
        User agentUser = null;
        if(usersWithAgentName != null && !usersWithAgentName.isEmpty()){
            if(usersWithAgentName.size() == 1){
                agentUser = usersWithAgentName.get(0);
            }
        }
        */
        if(accountName != null){
            User agentUser = userManager.findById(dbContext, accountName);
            if(agentUser != null){
                agent.setUser(agentUser);
            }
        }


        manager.createOrUpdate(dbContext, agent);
        return agent;
    }


    public static XapiAgent makeFromJson(Object dbContext, JSONObject agentJson) {
        return null;
    }


}
