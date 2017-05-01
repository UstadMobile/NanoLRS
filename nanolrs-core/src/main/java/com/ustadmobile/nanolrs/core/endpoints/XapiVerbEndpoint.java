package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 15/11/16.
 */

public class XapiVerbEndpoint {

    public static XapiVerb createOrUpdate(Object dbContext, JSONObject verbJSON) {
        try {
            XapiVerbManager verbManager = PersistenceManager.getInstance().getManager(XapiVerbManager.class);
            String verbID = verbJSON.getString("id");
            XapiVerb verb = verbManager.findById(dbContext, verbID);
            if(verb== null) {
                verb = verbManager.make(dbContext, verbID);
                verb.setCanonicalData(verbJSON.toString());
            }else {
                JSONObject storedJSON = new JSONObject(verb.getCanonicalData());
                JsonUtil.mergeJson(verbJSON, storedJSON);
                verb.setCanonicalData(storedJSON.toString());
            }
            verbManager.persist(dbContext, verb);

            return verb;
        }catch(JSONException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }

    }

}
