package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiActivityManager;
import com.ustadmobile.nanolrs.core.model.XapiActivityProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by mike on 10/2/16.
 */

public class XapiActivityEndpoint {

    /**
     * As per the Xapi spec: create or update the given object
     *
     * @param object
     * @param dbContext
     * @return
     */
    public static XapiActivityProxy createOrUpdate(Object dbContext, JSONObject object) {
        XapiActivityManager manager = PersistenceManager.getInstance().getActivityManager();
        try {
            String activityId = object.getString("id");
            XapiActivityProxy data = manager.findById(dbContext, activityId);
            if(data == null) {
                data = manager.makeNew(dbContext);
                data.setActivityId(activityId);
            }

            String jsonDef = data.getCanonicalData();
            JSONObject storedObject;
            if(jsonDef == null) {
                storedObject = new JSONObject();
            }else {
                storedObject = new JSONObject(jsonDef);
            }

            JsonUtil.mergeJson(object, storedObject);
            data.setCanonicalData(storedObject.toString());
            manager.createOrUpdate(dbContext, data);

            return data;
        }catch(JSONException e) {
            throw new IllegalArgumentException("No id in activity JSON object", e);
        }
    }

    public static XapiActivityProxy createOrUpdate(Object dbContext, String activityId) {
        try {
            JSONObject activityObj = new JSONObject();
            activityObj.put("id", activityId);
            return createOrUpdate(dbContext, activityObj);
        }catch(JSONException e) {
            //this should never happen- setting one string property
            throw new IllegalArgumentException(e);
        }

    }


}
