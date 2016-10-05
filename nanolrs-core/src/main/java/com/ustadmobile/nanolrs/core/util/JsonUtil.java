package com.ustadmobile.nanolrs.core.util;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by mike on 10/3/16.
 */

public class JsonUtil {

    /**
     * Recursively merge 2 JSON Objects
     *
     * @param source
     * @param dest
     */
    public static void mergeJson(JSONObject source, JSONObject dest) {
        Iterator<String> keys = source.keys();
        Object destProp;
        Object srcProp;
        String key;
        while(keys.hasNext()) {
            key = keys.next();
            srcProp = source.get(key);
            destProp = dest.has(key) ? dest.get(key) : null;

            if(destProp instanceof JSONObject && srcProp instanceof JSONObject) {
                mergeJson((JSONObject)srcProp, (JSONObject)destProp);
            }else {
                dest.put(key, source.get(key));
            }
        }
    }

}
