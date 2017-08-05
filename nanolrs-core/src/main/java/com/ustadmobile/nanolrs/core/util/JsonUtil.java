package com.ustadmobile.nanolrs.core.util;

import org.json.JSONArray;
import org.json.JSONException;
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
        try {
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
        }catch(JSONException e) {
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * Checks if a given string can be serializable to JSON
     * @param jsonString
     * @return
     */
    public static boolean isThisStringJSON(String jsonString){
        try {
            new JSONObject(jsonString);
        } catch (JSONException ex) {
            try {
                new JSONArray(jsonString);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add two JSON Arrays and return it
     * @param ja1
     * @param ja2
     * @return
     */
    public static JSONArray addTheseTwoJSONArrays(JSONArray ja1, JSONArray ja2){
        for(int i=0;i<ja2.length();i++){
            ja1.put(ja2.get(i));
        }
        return ja1;
    }

}
