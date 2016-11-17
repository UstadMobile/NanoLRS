package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.XapiVerbProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by mike on 15/11/16.
 */

public class XapiVerbEndpoint {

    public static XapiVerbProxy createOrUpdate(Object dbContext, JSONObject verbJSON) {
        XapiVerbManager verbManager = PersistenceManager.getInstance().getVerbManager();
        String verbID = verbJSON.getString("id");
        XapiVerbProxy verb = verbManager.findById(dbContext, verbID);
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
    }

}
