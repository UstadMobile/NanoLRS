package com.ustadmobile.nanolrs.core.sync;

import org.json.JSONObject;

/**
 * Created by varuna on 9/4/2017.
 */

public class UMSyncInfo {

    public static final String ENTITY_INFO_CLASS_NAME = "pCls";
    public static final String ENTITY_INFO_TABLE_NAME = "tableName";
    public static final String ENTITY_INFO_COUNT = "count";
    public static final String ENTITY_INFO_PRIMARY_KEY = "pk";

    private Class entityClass;
    private int count;

    public UMSyncInfo(Class entityClass, int count) {
        this.entityClass = entityClass;
        this.count = count;
    }

    /**
     * Constructor to Get UMSyncInfo from json representation
     * @param infoJSON  The json

     * @throws ClassNotFoundException If class not found..
     */
    public UMSyncInfo(JSONObject infoJSON) throws ClassNotFoundException {
        String className = infoJSON.getString(ENTITY_INFO_CLASS_NAME);
        String tableNAme = infoJSON.getString(ENTITY_INFO_TABLE_NAME);
        int count = Integer.parseInt(infoJSON.getString(ENTITY_INFO_COUNT));
        String pkField = infoJSON.getString(ENTITY_INFO_PRIMARY_KEY);

        Class entityClass = Class.forName(className);

        this.entityClass = entityClass;
        this.count = count;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(int addCount){
        this.count = this.count + addCount;
    }

    public JSONObject toJSON(){
        JSONObject infoJSON = new JSONObject();
        String className = entityClass.getName();
        String tableName = UMSyncEndpoint.getTableNameFromClass(entityClass);
        String pkField = UMSyncEndpoint.getPrimaryKeyFromClass(entityClass);
        infoJSON.put(ENTITY_INFO_CLASS_NAME, className);
        infoJSON.put(ENTITY_INFO_TABLE_NAME, tableName);
        infoJSON.put(ENTITY_INFO_COUNT, count);
        infoJSON.put(ENTITY_INFO_PRIMARY_KEY, pkField);
        return infoJSON;
    }

    /**
     * Get UMSyncInfo from json representation
     * @param infoJSON  The json
     * @return  UMSyncInfo object
     * @throws ClassNotFoundException If class not found..
     */
    public UMSyncInfo fromJSON(JSONObject infoJSON) throws ClassNotFoundException {
        String className = infoJSON.getString(ENTITY_INFO_CLASS_NAME);
        String tableNAme = infoJSON.getString(ENTITY_INFO_TABLE_NAME);
        int count = Integer.parseInt(infoJSON.getString(ENTITY_INFO_COUNT));
        String pkField = infoJSON.getString(ENTITY_INFO_PRIMARY_KEY);

        Class entityClass = Class.forName(className);

        UMSyncInfo thisInfo = new UMSyncInfo(entityClass, count);
        return thisInfo;
    }
}
