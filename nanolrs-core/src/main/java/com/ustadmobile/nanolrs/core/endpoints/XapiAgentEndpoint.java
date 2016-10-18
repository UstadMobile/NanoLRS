package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.XapiAgentProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 10/6/16.
 */

public class XapiAgentEndpoint {

    public static XapiAgentProxy createOrUpdate(Object dbContext, JSONObject agentJSON) {
        String mbox = agentJSON.optString("mbox", null);
        String accountHomepage = null;
        String accountName = null;
        if(agentJSON.has("account")) {
            JSONObject accountObj = agentJSON.getJSONObject("account");
            accountHomepage = accountObj.getString("homePage");
            accountName = accountObj.getString("name");
        }

        XapiAgentManager manager = PersistenceManager.getInstance().getAgentManager();
        List<XapiAgentProxy> matchingAgents = manager.findAgentByParams(
                dbContext, mbox, accountName, accountHomepage);

        if(matchingAgents != null && matchingAgents.size() > 0) {
            return matchingAgents.get(0);
        }

        //does not exist - needs to be created
        XapiAgentProxy agent = manager.makeNew(dbContext);
        agent.setId(UUID.randomUUID().toString());
        agent.setMbox(mbox);
        agent.setAccountHomepage(accountHomepage);
        agent.setAccountName(accountName);
        manager.createOrUpdate(dbContext, agent);

        return agent;
    }

    public static XapiAgentProxy makeFromJson(Object dbContext, JSONObject agentJson) {
        return null;
    }


}
