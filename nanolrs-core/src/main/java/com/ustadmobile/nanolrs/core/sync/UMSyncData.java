package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by varuna on 9/1/2017.
 */

public class UMSyncData {

    private List<NanoLrsModel> entities;
    private List<UMSyncInfo> info;
    private List<NanoLrsModel> conflicts;

    //Constructors
    public UMSyncData(List<NanoLrsModel> entities) {
        this.entities = entities;
        this.info = new ArrayList<>();
        this.conflicts = new ArrayList<>();
    }
    public UMSyncData(List<NanoLrsModel> entities, List<UMSyncInfo> info){
        this.entities = entities;
        this.info = info;
        this.conflicts = new ArrayList<>();
    }
    /**
     * Updates entities from sync JSON in an incoming sync
     * @param syncJSON  The sync formatted JSON
     * @return
     */
    public UMSyncData(JSONObject syncJSON, Object dbContext) throws ClassNotFoundException {

        //Get data and info separately
        JSONArray entitiesJSON = syncJSON.getJSONArray(UMSyncEndpoint.RESPONSE_ENTITIES_DATA);
        JSONArray entitiesInfoJSON = syncJSON.getJSONArray(UMSyncEndpoint.RESPONSE_ENTITIES_INFO);
        this.entities = new ArrayList<>();
        this.info = new ArrayList<>();
        setInfo(entitiesInfoJSON);
        setEntities(entitiesJSON, dbContext);
    }

    //Getters and Setters
    public List<NanoLrsModel> getEntities() {
        return entities;
    }
    public void setEntities(List<NanoLrsModel> entities) {
        this.entities = entities;
    }
    /**
     * Converts entitiesJSON to Entities mapped to their entity proxy class
     * @param entitiesJSON
     * @param dbContext
     * @return
     */
    public void setEntities(JSONArray entitiesJSON, Object dbContext){

        List<NanoLrsModel> allNewEntitiesMap = null;
        for(int i=0; i < entitiesJSON.length(); i++){
            System.out.println(" -->JSON->Object");
            JSONObject entityJSON = entitiesJSON.getJSONObject(i);
            NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
            allNewEntitiesMap.add(thisEntity);
            System.out.println("   ->OK.");
        }
        this.entities = allNewEntitiesMap;
    }

    public List<UMSyncInfo> getInfo() {
        return info;
    }
    public void setInfo(List<UMSyncInfo> info) {
        this.info = info;
    }

    /**
     * Update info with new info with count.
     * @param thisType
     * @param thisCount
     */
    public void addInfo(Class thisType, int thisCount){
        UMSyncInfo thisInfo = new UMSyncInfo(thisType, thisCount);
        info.add(thisInfo);
    }

    /**
     * Gets the info as JSONArray
     * @return
     */
    public JSONArray getInfoJSON(){
        JSONArray infoJSON = new JSONArray();
        Iterator<UMSyncInfo> infoIterator = info.iterator();
        while(infoIterator.hasNext()){
            UMSyncInfo thisInfo = infoIterator.next();
            infoJSON.put(thisInfo.toJSON());
        }
        return infoJSON;
    }

    /** Set info from its json array representation
     *
     * @param infoJSON
     * @throws ClassNotFoundException
     */
    public void setInfo(JSONArray infoJSON) throws ClassNotFoundException {
        for(int i=0; i<infoJSON.length(); i++){
            JSONObject thisInfoJSON = infoJSON.getJSONObject(i);
            UMSyncInfo thisInfo = new UMSyncInfo(thisInfoJSON);
            info.add(thisInfo);
        }
    }

    public List<NanoLrsModel> getConflicts() {
        return conflicts;
    }
    public void setConflicts(List<NanoLrsModel> conflicts) {
        this.conflicts = conflicts;
    }

    /**
     * Get Proxy class from Entity given.
     * @param entity
     * @return
     */
    public Class getProxyFromEntity(NanoLrsModelSyncable entity){
        if(entity.getClass().getInterfaces() == null){
            return null;
        }
        Class proxy = entity.getClass().getInterfaces()[0];
        Class<?>[] allInterfaces = entity.getClass().getInterfaces();
        for(Class thisInterface:allInterfaces){
            if(thisInterface.getName().contains("model")){
                proxy = thisInterface;
                break; //I want to break free..
            }
        }
        return proxy;
    }
    /**
     * Crates a map of the entities in JSON Array and their Info summed up.
     * @return  Sync JSON Object with data in data and info in info keys
     */
    public JSONObject toSyncJSON(){

        JSONArray entitiesData = new JSONArray();
        JSONArray entitiesInfo = new JSONArray();
        Map<Class, Integer> typeToCount = new HashMap<>();

        //Create the data
        List<NanoLrsModel> pendingEntitesToBeSynced = this.entities;
        if(pendingEntitesToBeSynced != null && !pendingEntitesToBeSynced.isEmpty()){
            Iterator<NanoLrsModel> pendingEntitesIterator = pendingEntitesToBeSynced.iterator();
            while(pendingEntitesIterator.hasNext()){
                NanoLrsModelSyncable thisEntity =
                        (NanoLrsModelSyncable)pendingEntitesIterator.next();

                //Get the entity type from thisEntity:
                //TODODone: Make this better than just getting the first one..
                Class thisEntityProxyType = getProxyFromEntity(thisEntity);

                JSONObject thisEntityInJSON =
                        ProxyJsonSerializer.toJson(thisEntity, thisEntityProxyType);
                entitiesData.put(thisEntityInJSON);

                if(typeToCount.get(thisEntityProxyType) == null){
                    typeToCount.put(thisEntityProxyType, 1);
                }else{
                    int updatedCount = typeToCount.get(thisEntityProxyType) + 1;
                    typeToCount.put(thisEntityProxyType, updatedCount);
                }
            }
        }

        //Create the info
        Iterator<Map.Entry<Class, Integer>> typeToCountIterator = typeToCount.entrySet().iterator();
        while(typeToCountIterator.hasNext()){
            Map.Entry<Class, Integer> thisEntry = typeToCountIterator.next();
            Class thisType = thisEntry.getKey();
            int thisCount = thisEntry.getValue();
            addInfo(thisType, thisCount);
        }
        entitiesInfo = getInfoJSON();

        //Add them together:
        JSONObject entitiesDataInfo = new JSONObject();
        entitiesDataInfo.put(UMSyncEndpoint.RESPONSE_ENTITIES_INFO, entitiesInfo);
        entitiesDataInfo.put(UMSyncEndpoint.RESPONSE_ENTITIES_DATA, entitiesData);

        return entitiesDataInfo;
    }

}
