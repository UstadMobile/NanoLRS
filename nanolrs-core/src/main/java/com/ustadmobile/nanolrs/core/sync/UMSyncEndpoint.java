package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.NodeSyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.mapping.ModelManagerMapping;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.NodeSyncStatus;
import com.ustadmobile.nanolrs.core.model.SyncStatus;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.Base64CoderNanoLrs;
import com.ustadmobile.nanolrs.core.util.JsonUtil;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This sync endpoint is responsible for syncing databases inter-between servers or other UstadMobile
 * instances (nodes). Sync is initiated on client and is communicated between other UstadMobile devices
 * and servers via HTTP request.
 *
 * Created by varuna on 6/27/2017.
 */
public class UMSyncEndpoint {

    public static final String REQUEST_CONTENT_LENGTH = "Content-Length";
    public static final String REQUEST_CONTENT_TYPE = "Content-Type";
    public static final String REQUEST_ACCEPT = "Accept";
    public static final String REQUEST_AUTHORIZATION = "Authorization";

    //Custom headers starting with X-UM-..
    public static final String HEADER_NODE_NAME = "X-UM-nodename";
    public static final String HEADER_NODE_HOST = "X-UM-nodehost";
    public static final String HEADER_NODE_URL = "X-UM-nodeurl";
    public static final String HEADER_NODE_UUID = "X-UM-nodeuuid";
    public static final String HEADER_NODE_ROLE = "X-UM-noderole";
    public static final String HEADER_NODE_ISMASTER = "X-UM-nodeismaster";
    public static final String HEADER_NODE_ISPROXY = "X-UM-nodeisproxy";

    public static final String HEADER_USER_USERNAME = "X-UM-username";
    public static final String HEADER_USER_PASSWORD = "X-UM-password";
    public static final String HEADER_USER_UUID = "X-UM-useruuid";
    public static final String HEADER_USER_IS_NEW = "X-UM-isnewuser";

    public static final String RESPONSE_CHANGE_USERNAME = "X-UM-changeusernameto";
    public static final String RESPONSE_SEND_USER_AGAIN = "X-UM-senduseragain";
    public static final String RESPONSE_SYNCED_STATUS = "X-UM-syncstatus";

    public static final String RESPONSE_USER_JUST_CREATED = "X-UM-userjustcreated";

    public static final String RESPONSE_SYNC_OK = "OK";
    public static final String RESPONSE_SYNC_FAIL = "FAIL";

    public static final String RESPONSE_ENTITIES_DATA = "data";
    public static final String RESPONSE_ENTITIES_INFO = "info";
    public static final String RESPONSE_CONFLICT = "conflict";

    public static final String ENTITY_INFO_CLASS_NAME = "pCls";
    public static final String ENTITY_INFO_TABLE_NAME = "tableName";
    public static final String ENTITY_INFO_COUNT = "count";
    public static final String ENTITY_INFO_PRIMARY_KEY = "pk";

    public static final String JSON_MIMETYPE = "application/json";
    public static final String UTF_ENCODING = "UTF-8";

    /**
     * Common method to return primary key field name when supplied a class entity
     *
     * @param syncableEntity    the Class. eg: User.class
     * @return String           the primary key field name. eg: "username"
     */
    public static String getPrimaryKeyFromClass(Class syncableEntity){
        Method[] allEntityMethods = syncableEntity.getMethods();
        String pkMethod = null;
        String pkField;
        for(Method method : allEntityMethods) {
            if(method.isAnnotationPresent(PrimaryKeyAnnotationClass.class)) {
                pkMethod = method.getName();
                break;
            }
        }
        int prefixLen = 0;
        if(pkMethod.startsWith("is"))
            prefixLen = 2;
        else if(pkMethod.startsWith("get"))
            prefixLen = 3;
        pkField = Character.toLowerCase(pkMethod.charAt(3)) +
                pkMethod.substring(prefixLen+1);
        return pkField;
    }

    /**
     * Common method to return primary key method name when supplied a class entity
     *
     * @param syncableEntity    the Class. eg: User.class
     * @return String           primary key method. eg: "getUsername"
     */
    public static String getPrimaryKeyMethodFromClass(Class syncableEntity){
        Method[] allEntityMethods = syncableEntity.getMethods();
        String pkMethod = null;
        for(Method method : allEntityMethods) {
            if(method.isAnnotationPresent(PrimaryKeyAnnotationClass.class)) {
                pkMethod = method.getName();
                break;
            }
        }
        return pkMethod;
    }

    /**
     * Common method to return entity manager from a proxy class (of that entity)
     *
     * @param syncableEntity            The Entity's proxy class. eg: User.class
     * @return  NanoLrsManagerSyncable  The Entity's Manager class. eg: UserManager.class
     */
    public static NanoLrsManagerSyncable getManagerFromProxyClass(Class syncableEntity){
        Class managerClass = ModelManagerMapping.proxyClassToManagerMap.get(syncableEntity);
        NanoLrsManagerSyncable syncableEntityManager = (NanoLrsManagerSyncable)
                PersistenceManager.getInstance().getManager(managerClass);
        return syncableEntityManager;
    }

    /**
     * Common method to get entity's manager from a string proxy class name (of that entity)
     *
     * @param thisProxyClassName        The Entity's proxy class name. eg: "User"
     * @return NanoLrsManagerSyncable   The Entity's Manager class. eg: UserManager.class
     */
    public static NanoLrsManagerSyncable getManagerFromProxyName(String thisProxyClassName){
        Class thisProxyClass = ModelManagerMapping.proxyNameToClassMap.get(thisProxyClassName);
        return getManagerFromProxyClass(thisProxyClass);
    }

    /**
     * Common method to return the table name from Proxy Class. Note that table names
     *  are upper cased for consistency.
     *
     * @param syncableEntity    The Entity's proxy class. eg: User.class
     * @return String           The Entity's table name (in database). eg: "USER"
     */
    public static String getTableNameFromClass(Class syncableEntity){
        String tableName = convertCamelCaseNameToUnderscored(
                Character.toLowerCase(syncableEntity.getSimpleName().charAt(0)) +
                        syncableEntity.getSimpleName().substring(1));
        if(tableName != null && !tableName.isEmpty()){
            tableName = tableName.toUpperCase();
        }
        return tableName;
    }

    /**
     * Common method to convert input-stream to string with encoding provided.
     *
     * @param is                InputStream
     * @param encoding          encoding
     * @return String           The string from the InputStream.
     * @throws IOException      Because we are doing I/O operations.
     */
    public static String convertStreamToString2(InputStream is, String encoding) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, encoding);
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    /**
     * Gets all syncable entities' next change seq number and stores it in a map against entity.
     *
     * @param dbContext               The databse context.
     * @return Map<Class, Long>       Map of every class and its' next change seq number.
     * @throws SQLException
     */
    public static Map<Class, Long> getAllEntitiesSeqNum(Object dbContext) throws SQLException {
        Map<Class, Long> allEntitiesSeqMap = new HashMap<>();
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);

        for(Class thisEntity:ModelManagerMapping.SYNCABLE_ENTITIES){
            //Pre-Sync : Add existing ChangeSeq value to preSyncAllEntitiesSeqMap
            String tableName = getTableNameFromClass(thisEntity);
            long preSyncEntitySeqNum =
                    changeSeqManager.getNextChangeByTableName(tableName, dbContext);
            allEntitiesSeqMap.put(thisEntity, preSyncEntitySeqNum);
        }
        return allEntitiesSeqMap;
    }


    /**
     * Converts entitiesJSON to Entities mapped to their entity proxy class.
     *
     * @param entitiesJSON      The JSON Array to convert to entities
     * @param dbContext         The database connection source
     * @return  Map             The map of entities with their proxy class name.
     *                  eg:
     *                      <User1 object, "com.ustadmobile.nanolrs.core.model.User";
     *                      Statement1 object, "com.ustadmobile.nanolrs.core.model.XapiStatement";
     *                      ...>
     */
    public static Map<NanoLrsModelSyncable, String> entitiesJSONToEntitiesMap(
            JSONArray entitiesJSON, Object dbContext){
        Map<NanoLrsModelSyncable, String> allNewEntitiesMap = new HashMap<>();
        //Create Entity Map of <Entity Object, Proxy Class Name>
        for(int i=0; i < entitiesJSON.length(); i++){
            JSONObject entityJSON = entitiesJSON.getJSONObject(i);
            NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
            String thisProxyClass =
                    entityJSON.getString(ProxyJsonSerializer.PROXY_CLASS_JSON_FIELD);
            allNewEntitiesMap.put((NanoLrsModelSyncable)thisEntity, thisProxyClass);
        }
        return allNewEntitiesMap;
    }

    /**
     * Update changeSeq with increment based on Entities info JSON, return preSync Map for every
     * entity.
     *
     * @param entitiesInfoJSON      The entities info JSON that has the count
     * @param dbContext             The database context
     * @return  Map<String, Long>   Map containing presync change Seq frr every entity type
     * @throws SQLException
     */
    public static Map<String, Long> getEntityChangeSeqAndIncrementItForInfo(
            JSONArray entitiesInfoJSON, Object dbContext) throws SQLException {

        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);
        Map<String, Long> preSyncEntitySeqNumMap = new HashMap<>();
        //Reserve set of ChangeSeq numbers for every entity type
        //Increment every Entity's ChangeSeq by count of new updates
        for(int j=0;j<entitiesInfoJSON.length();j++){
            JSONObject thisEntityInfoJSON = entitiesInfoJSON.getJSONObject(j);
            String proxyClassName = thisEntityInfoJSON.getString(ENTITY_INFO_CLASS_NAME);
            String tableName = thisEntityInfoJSON.getString(ENTITY_INFO_TABLE_NAME);
            tableName = tableName.toUpperCase();
            int count = thisEntityInfoJSON.getInt(ENTITY_INFO_COUNT);

            //Pre-Sync : Add existing ChangeSeq value to preSyncEntitySeqNumMap
            long preSyncEntitySeqNum =
                    changeSeqManager.getNextChangeByTableName(tableName, dbContext);
            preSyncEntitySeqNumMap.put(proxyClassName, preSyncEntitySeqNum);

            //Increment the ChangeSeq by count of new & update entities
            changeSeqManager.getNextChangeAddSeqByTableName(tableName, count, dbContext);
        }

        return preSyncEntitySeqNumMap;
    }

    /**
     * Returns an empty UMSyncResult with the given status code.
     *
     * @param resultStatus  The result status code.
     * @return  UMSyncResult object which is empty except for status code given.
     * @throws UnsupportedEncodingException
     */
    public static UMSyncResult returnEmptyUMSyncResult(int resultStatus)
            throws UnsupportedEncodingException {
        Map responseHeaders = new HashMap();
        return returnEmptyUMSyncResultWithHeader(resultStatus, responseHeaders);
    }

    /**
     * Returns an empty UMSyncResult with the given status code and headers.
     *
     * @param resultStatus          The result status code.
     * @param responseHeaders       Map of headers to associate UMSyncResult object
     * @return UMSyncResult         object which is empty except for code and headers.
     * @throws UnsupportedEncodingException
     */
    public static UMSyncResult returnEmptyUMSyncResultWithHeader(int resultStatus, Map responseHeaders)
            throws UnsupportedEncodingException {
        String emptyResponseString = "";
        InputStream responseData = new ByteArrayInputStream(emptyResponseString.getBytes(UTF_ENCODING));
        long responseLength = 0;
        UMSyncResult resultResponse = new UMSyncResult(resultStatus,responseHeaders,
                responseData, responseLength);

        return resultResponse;
    }

    /**
     * Common method that converts name from camel case to snake case. eg: fullName to full_name .
     *
     * @param propertyName Property Name e.g. propertyName
     * @return Property named in lower case separated by underscores e.g. property_name
     */
    public static String convertCamelCaseNameToUnderscored(String propertyName) {
        String underScoredName = "";
        for(int i = 0; i < propertyName.length(); i++) {
            if(Character.isUpperCase(propertyName.charAt(i)) &&
                    (i == 0 || Character.isLowerCase(propertyName.charAt(i-1)))) {
                underScoredName += "_";
            }
            underScoredName += Character.toLowerCase(propertyName.charAt(i));
        }
        return underScoredName;
    }

    /**
     * Creates a JOSNInfo Object from class with count.
     *
     * @param syncableEntity    The Entity proxy class. eg: User.class
     * @param count             The count of this type of entity. eg: 42
     * @return JSONObject
     * eg:
     *
     *          {"pCls":"com.ustadmobile.nanolrs.core.model.User",
     *          "tableName":"USER",
     *          "count":42,
     *          "pk":"username"}
     *
     */
    public static JSONObject createJSONInfoFromClass(Class syncableEntity, int count){
        JSONObject thisEntityInfo = new JSONObject();
        String tableName = getTableNameFromClass(syncableEntity);
        String pkField = getPrimaryKeyFromClass(syncableEntity);
        thisEntityInfo.put(ENTITY_INFO_CLASS_NAME, syncableEntity.getName());
        thisEntityInfo.put(ENTITY_INFO_TABLE_NAME, tableName);
        thisEntityInfo.put(ENTITY_INFO_COUNT, count);
        thisEntityInfo.put(ENTITY_INFO_PRIMARY_KEY, pkField);

        return thisEntityInfo;
    }

    /**
     * Gets the latest seqNum for an array of one entity typed array list. To be used for only
     *  one type of entity specified.
     *
     * @param pendingEntitesToBeSynced  List of entities pending to be synced (of one type).
     *                                  eg: <User3, User88, User42, User23>
     * @return  long    latest seq Number for that entity type. eg: 89
     */
    public static long getLatestSeqNumFromEntityArray(List<NanoLrsModel> pendingEntitesToBeSynced){
        long latestSeqNum = -1;
        if(pendingEntitesToBeSynced != null && !pendingEntitesToBeSynced.isEmpty()) {
            Iterator<NanoLrsModel> pendingEntitesIterator = pendingEntitesToBeSynced.iterator();
            while (pendingEntitesIterator.hasNext()) {
                NanoLrsModelSyncable thisEntity =
                        (NanoLrsModelSyncable) pendingEntitesIterator.next();

                //Update latestSeqNum given back:
                if(latestSeqNum == -1){
                    latestSeqNum = thisEntity.getLocalSequence();
                }else{
                    if(latestSeqNum < thisEntity.getLocalSequence()){
                        latestSeqNum = thisEntity.getLocalSequence();
                    }
                }
            }
        }
        if(latestSeqNum == -1){
            return 0;
        }else{
            return latestSeqNum;
        }
    }

    /**
     * Crates a map of the entities in JSON Array and their Info summed up. To be used for only
     * one type of Entitiy specified.
     *
     * @param pendingEntitesToBeSynced  List of entities to create json data (Only of one type)
     * @param syncableEntity    The type of entity specified. eg: User.class
     * @return  Map.Entry of data and info.
     */
    public static Map.Entry<JSONArray, JSONObject> createJSONDataFromEntityArray(List<NanoLrsModel>
                                                 pendingEntitesToBeSynced, Class syncableEntity){

        int count = 0;
        long latestSeqNumToUpdateSyncStatus = -1;
        JSONArray entitiesData = new JSONArray();
        JSONObject thisEntityInfo = new JSONObject();
        Map<JSONArray, JSONObject> entitiesDataInfoMap = new HashMap<>();

        if(pendingEntitesToBeSynced != null && !pendingEntitesToBeSynced.isEmpty()){
            Iterator<NanoLrsModel> pendingEntitesIterator = pendingEntitesToBeSynced.iterator();
            while(pendingEntitesIterator.hasNext()){
                NanoLrsModelSyncable thisEntity =
                        (NanoLrsModelSyncable)pendingEntitesIterator.next();

                JSONObject thisEntityInJSON =
                        ProxyJsonSerializer.toJson(thisEntity, syncableEntity);
                entitiesData.put(thisEntityInJSON);
                count = count + 1;
            }
        }
        thisEntityInfo = createJSONInfoFromClass(syncableEntity, count);

        entitiesDataInfoMap.put(entitiesData, thisEntityInfo);

        Map.Entry<JSONArray, JSONObject> entitiesDataInfoEntry = entitiesDataInfoMap.entrySet().iterator().next();
        return entitiesDataInfoEntry;
    }

    /**
     * Creates headers needed for sync request. Defaults password to user's password.
     * This is most likely a hash. This method shouldn't be used. Keeping it for old version.
     * TODO: Get rid of me and my mentions.
     *
     * @param user  The user making the sync request.
     * @param node  The node making the sync request.
     * @return Map of headers and values.
     */
    public static Map <String, String> createSyncHeader(User user, Node node){
        return createSyncHeader(user, user.getPassword(), node);
    }

    /**
     * Creates headers needed for sync request.
     *
     * @param user  The user making the sync request.
     * @param cred  The password in plain text.
     * @param node  The node making the sync request.
     * @return Map of headers and values.
     */
    public static Map <String, String> createSyncHeader(User user, String cred, Node node){
        //Headers if any..
        Map <String, String> headers = new HashMap<String, String>();
        if(user != null) {
            headers.put(HEADER_USER_USERNAME, user.getUsername());
            headers.put(HEADER_USER_PASSWORD, cred);
            headers.put(HEADER_USER_UUID, user.getUuid());
            String isNewUser = "false";
            //Watch out: master will always go to 0 if there is a local update.
            //So, we need to set master to -1 when we register new users (in system impl SE)
            if(user.getMasterSequence() < 0 ) { // ie -1
                isNewUser = "true";
            }
            headers.put(HEADER_USER_IS_NEW, isNewUser);
        }

        if(node != null) {
            headers.put(HEADER_NODE_UUID, node.getUUID());
            headers.put(HEADER_NODE_HOST, node.getHost());
            headers.put(HEADER_NODE_URL, node.getUrl());
            //mostly its "client" as they are the ones that start sync.
            //However that could change, so sending role.
            //TODODone: we need to validate these roles somehow
            //Update: Validate on other side receiving not sending..
            //mayb: tokens that get authorised like certificates.
            String thisNodeRole = "client";
            if (node.isMaster()) {
                thisNodeRole = "master";
            }
            if (node.isProxy()) {
                thisNodeRole = "proxy";
            }
            headers.put(HEADER_NODE_ROLE, thisNodeRole);

            headers.put(RESPONSE_SYNCED_STATUS, RESPONSE_SYNC_OK);
        }
        return headers;
    }

    /**
     * Creates parameters needed for sync request. (doesnt do anything now).
     *
     * @param user The user making the sync request.
     * @param node The node making the sync request.
     * @return
     */
    public static Map<String, String> createSyncParameters(User user, Node node){
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("someparameter", "somevalue");
        return parameters;
    }

    /**
     * Sets headers to the connection from a given header Map.
     *
     * @param connection    The connection where we set the headers.
     * @param headers       A map of headers with values.
     * @throws IOException
     */
    private static void setHeaders(HttpURLConnection connection, Map headers) throws IOException {
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(pair.getKey() != null && pair.getValue() != null){
                connection.setRequestProperty(pair.getKey().toString(), pair.getValue().toString());
                it.remove(); // avoids a ConcurrentModificationException
            }

        }
    }

    /**
     * Find all pending entities needed to be synced for all entities at this moment, or since the
     * given changeSeq numbers map. Also give back the latest ChangeSeq mapping. This is so that we
     * can update it.
     * The reason in returning two objects as entry is if we separate it out, we may have an edge
     * case between methods of new entities.
     *
     * @param thisUser  The User starting the sync request.
     * @param node      The node starting the sync request.
     * @param fromSeq   From Sequence Number (if provided).
     * @param toSeq     To Sequence Number (if provided).
     * @param dbContext Database context.
     * @return Map.Entry of UMSyncData and entity type<->latest ChangeSeq mapping.
     * @throws SQLException
     * @throws IOException
     */
    public static  Map.Entry<UMSyncData, Map<Class, Long>> getSyncInfo (User thisUser,
                     Node node, Map<Class, Long> fromSeq, Map<Class, Long> toSeq, Object dbContext)
            throws SQLException, IOException {

        //Get managers
        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);

        //Map of Entity and latestSeq got so we can update sync status upon sync success
        Map<Class, Long> latestChangeSeqMap = new HashMap<>();

        List<NanoLrsModel> entities = new ArrayList<>();

        //Scan through every Syncable entity..
        for(Class syncableEntity : ModelManagerMapping.SYNCABLE_ENTITIES) {
            //Get its manager
            NanoLrsManagerSyncable syncableEntityManager = getManagerFromProxyClass(syncableEntity);

            //Get changeSeq from given map or find the latest one right now..
            long fromSyncSeq;
            long toSyncSeq;
            if(fromSeq != null && !fromSeq.isEmpty()){
                fromSyncSeq = fromSeq.get(syncableEntity);
            }else{
                fromSyncSeq =
                        syncStatusManager.getSentStatus(node.getHost(), syncableEntity, dbContext);
            }
            if(toSeq != null && !toSeq.isEmpty()){
                toSyncSeq = toSeq.get(syncableEntity);
            }else{
                toSyncSeq = -1;

            }

            //Get pendingEntities since the last sync status for this host.
            List<NanoLrsModel> pendingEntitesToBeSynced =
                    syncableEntityManager.getAllSinceTwoSequenceNumber(thisUser, node.getHost(),
                            fromSyncSeq, toSyncSeq, dbContext);

            //Latest changeseqNum for this Entity
            long latestSeqNumToUpdateSyncStatus = getLatestSeqNumFromEntityArray(pendingEntitesToBeSynced);

            //Add the latestSeqNum to this class in a map so upon sync success
            // we can update SyncStatus
            if(latestSeqNumToUpdateSyncStatus > 0) {
                latestChangeSeqMap.put(syncableEntity, latestSeqNumToUpdateSyncStatus);
            }

            //Update entities
            Iterator<NanoLrsModel> pendingIterator = pendingEntitesToBeSynced.iterator();
            while(pendingIterator.hasNext()){
                entities.add(pendingIterator.next());
            }
        }

        UMSyncData syncData = new UMSyncData(entities);
        //Return only one set value.
        Map<UMSyncData, Map<Class, Long>> syncInfoMap = new HashMap<>();
        syncInfoMap.put(syncData, latestChangeSeqMap);
        Map.Entry<UMSyncData, Map<Class, Long>> syncInfo =
                syncInfoMap.entrySet().iterator().next();

        return syncInfo;
    }

    /**
     * Find all pending JSON needed to be synced for all entities at this moment, or since the
     * given changeSeq numbers map.
     *
     * @param thisUser      The user starting the sync request.
     * @param node          The node starting the sync request.
     * @param fromSeq       From seq number (optional).
     * @param toSeq         To seq number (optional).
     * @param dbContext     Database context.
     * @return  Map.Entry of JSONObject of new entities and changeseq mapping as of right now.
     * @throws SQLException
     * @throws IOException
     */
    public static  Map.Entry<JSONObject, Map<Class, Long>> getNewEntriesJSON (User thisUser,
                         Node node, Map<Class, Long> fromSeq, Map<Class, Long> toSeq, Object dbContext)
            throws SQLException, IOException {

        //Map.Entry<JSONArray, JSONArray>
        //Get managers
        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);

        //Map of Entity and latestSeq got so we can update sync status upon sync success
        Map<Class, Long> entityToLatestLocalSeqNum = new HashMap<>();

        JSONArray pendingJSONEntites = new JSONArray(); //entities
        JSONArray pendingJSONInfo = new JSONArray(); //entities info
        JSONObject pendingEntitiesWithInfo = new JSONObject(); //entities with entities info

        //Scan through every Syncable entity..
        for(Class syncableEntity : ModelManagerMapping.SYNCABLE_ENTITIES) {
            //Get its manager
            NanoLrsManagerSyncable syncableEntityManager = getManagerFromProxyClass(syncableEntity);

            //Get changeSeq from given map or find the latest one right now..
            long fromSyncSeq;
            long toSyncSeq;
            if(fromSeq != null && !fromSeq.isEmpty()){
                fromSyncSeq = fromSeq.get(syncableEntity);
            }else{
                fromSyncSeq =
                        syncStatusManager.getSentStatus(node.getHost(), syncableEntity, dbContext);
            }
            if(toSeq != null && !toSeq.isEmpty()){
                toSyncSeq = toSeq.get(syncableEntity);
            }else{
                toSyncSeq = -1;

            }

            //Get pendingEntities since the last sync status for this host
            //TODODone: lingo of method name
            //Update: Ignoring..
            List<NanoLrsModel> pendingEntitesToBeSynced =
                    syncableEntityManager.getAllSinceTwoSequenceNumber(thisUser, node.getHost(),
                            fromSyncSeq, toSyncSeq, dbContext);

            Map.Entry<JSONArray, JSONObject> entityEntriesAndInfo =
                    createJSONDataFromEntityArray(pendingEntitesToBeSynced, syncableEntity);
            pendingJSONEntites = JsonUtil.addTheseTwoJSONArrays(pendingJSONEntites,
                    entityEntriesAndInfo.getKey());

            pendingJSONInfo.put(entityEntriesAndInfo.getValue());

            long latestSeqNumToUpdateSyncStatus = getLatestSeqNumFromEntityArray(pendingEntitesToBeSynced);

            //Add the latestSeqNum to this class in a map so upon sync success
            // we can update SyncStatus
            if(latestSeqNumToUpdateSyncStatus > 0) {
                entityToLatestLocalSeqNum.put(syncableEntity, latestSeqNumToUpdateSyncStatus);
            }
        }

        //Create a JSONObject with entities JSONArray and info JSONArray
        pendingEntitiesWithInfo.put(RESPONSE_ENTITIES_DATA, pendingJSONEntites);
        pendingEntitiesWithInfo.put(RESPONSE_ENTITIES_INFO, pendingJSONInfo);

        Map<JSONObject, Map<Class, Long>> newEntitiesJSONAndLatestSeqNum = new HashMap<>();
        newEntitiesJSONAndLatestSeqNum.put(pendingEntitiesWithInfo, entityToLatestLocalSeqNum);
        Map.Entry<JSONObject, Map<Class, Long>> newEntitiesJSONAndLatestSeqNumEntry =
                newEntitiesJSONAndLatestSeqNum.entrySet().iterator().next();


        return newEntitiesJSONAndLatestSeqNumEntry;
    }

    /**
     * Get response headers from HttpURLConnection object.
     *
     * @param conn  HttpURLConnection object
     * @return  Map of response headers
     */
    public static Map<String, String> getHeadersFromRequest(HttpURLConnection conn){
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entries : conn.getHeaderFields().entrySet()) {
            String values = "";
            for (String value : entries.getValue()) {
                values += value + ",";
                headers.put(entries.getKey(), value);
            }
        }
        return headers;
    }

    /**
     * encode username password string to basic auth
     * @param username
     * @param password
     * @return
     */
    public static String encodeBasicAuth(String username, String password) {
        return "Basic " + Base64CoderNanoLrs.encodeString(username +
                ':' + password);
    }

    /**
     * Makes Sync Request with given JSON, headers, etc. Returns a UMSyncResult object.
     *
     * @param destURL       The endpoint url
     * @param method        The method type (usually POST)
     * @param username      The username for basic auth.
     * @param password      The password for basic auth.
     * @param headers       The request headers.
     * @param parameters    The request parameters (if any).
     * @param dataJSON      The JSON to sent as part of the request.
     * @param contentType   The content type to send as part of the request.
     * @param content       The database context.
     * @return UMSyncResult The Sync result as an object.
     */
    public static UMSyncResult makeSyncRequest(String destURL, String method, String username,
                                               String password, Map headers, Map parameters,
                                               JSONObject dataJSON, String contentType,
                                               byte[] content) {

        UMSyncResult response = new UMSyncResult();
        HttpURLConnection con = null;
        OutputStream out = null;

        //Get basic auth string for this username and password
        String basicAuthString = null;
        if(username != null && password != null){
            if(username.length() > 0 && password.length() > 0){
                basicAuthString = encodeBasicAuth(username, password);
                System.out.print("got basic: " + basicAuthString);
                headers.put(REQUEST_AUTHORIZATION, basicAuthString);
            }
        }

        try {
            URL url = new URL(destURL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method.toUpperCase());

            con.setDoOutput(true);
            if(!headers.isEmpty()){
                setHeaders(con, headers);
            }

            if(contentType != null) {
                con.setRequestProperty(REQUEST_CONTENT_TYPE, contentType);
                con.setRequestProperty(REQUEST_ACCEPT, contentType);
            }

            byte[] payload;
            if(dataJSON != null && dataJSON.length() > 0) {
                payload = dataJSON.toString().getBytes("UTF-8");
            }else if(content != null){
                payload = content;
            }else if(parameters != null && method.equalsIgnoreCase("POST")){
                String paramString = "";
                Iterator paramIterator = parameters.entrySet().iterator();
                while(paramIterator.hasNext()){
                    Map.Entry thisParameter = (Map.Entry)paramIterator.next();
                    String amp = "";
                    if(paramString != ""){
                        amp = "&";
                    }
                    paramString = paramString + amp + thisParameter.getKey() + "=" +
                            thisParameter.getValue();
                }
                payload = paramString.getBytes(UTF_ENCODING);
            }else {
                throw new IllegalArgumentException("Invalid arguments to makeSyncRequest");
            }

            con.setFixedLengthStreamingMode(payload.length);
            out = con.getOutputStream();
            out.write(payload);
            out.flush();

            int statusCode = con.getResponseCode();
            response.setHeaders(getHeadersFromRequest(con));
            response.setStatus(statusCode);
            response.setResponseData(con.getInputStream());
            response.setResponseLength(con.getContentLength());

        }catch(IOException e) {
            System.err.println("saveState Exception");
            e.printStackTrace();
        }finally {
            if(out != null) {
                try { out.close(); }
                catch(IOException e) {}
            }
        }
        return response;
    }

    /**
     * Persists json to database : Checks, updates, creates,  persists all JSON in given data
     * to database for a sender and receiver node.
     *
     * @param entitiesWithInfoJSON  The JSONObject that has data and info of the entities coming in.
     * @param senderNode            The sender's Node.
     * @param thisNode              The receiver's Node.
     * @param dbContext             The Database context.
     * @throws SQLException
     */
    public static boolean jsonToDB(JSONObject entitiesWithInfoJSON, Node senderNode, Node thisNode,
                                   User thisUser, Object dbContext)
            throws SQLException {
        boolean allgoood = false;
        SyncStatusManager syncStatusManager =
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);

        //Map of <Entity object,entity proxy class name>
        //Makes it easier to determine what type of entity object this is
        // and easier to get its manager.
        Map<NanoLrsModelSyncable, String> allNewEntitiesMap = new HashMap<>();

        //Get data and info separately
        JSONArray entitiesJSON = entitiesWithInfoJSON.getJSONArray(RESPONSE_ENTITIES_DATA);
        JSONArray entitiesInfoJSON = entitiesWithInfoJSON.getJSONArray(RESPONSE_ENTITIES_INFO);

        //Create Entity Map of <Entity Object, Proxy Class Name>
        allNewEntitiesMap =
                entitiesJSONToEntitiesMap(entitiesJSON, dbContext);

        //Reserve set of ChangeSeq numbers for every entity type
        //Increment every Entity's ChangeSeq by count of new updates
        Map<String, Long> preSyncEntitySeqNumMap =
                getEntityChangeSeqAndIncrementItForInfo(entitiesInfoJSON, dbContext);

        //Loop over the <Entities, Proxy Name> to add them to this DB
        //Persist without auto increment. We persist them in the gap between
        // Pre-Sync ChangeSeq and Post-Sync ChangeSeq(the one thats incremented)
        Iterator<Map.Entry<NanoLrsModelSyncable, String>> allNewEntitiesMapIterator =
                allNewEntitiesMap.entrySet().iterator();
        while(allNewEntitiesMapIterator.hasNext()){
            Map.Entry<NanoLrsModelSyncable, String> thisNewEntityMap =
                    (Map.Entry) allNewEntitiesMapIterator.next();
            //Get entity and its manager
            //TODO: You can get thisProxyClassName without the Map. :
            //thisNewEntity.getClass().getInterfaces()[0].getName()
            NanoLrsModelSyncable thisNewEntity = thisNewEntityMap.getKey();
            String thisProxyClassName = thisNewEntityMap.getValue();
            Class thisProxyClass = ModelManagerMapping.proxyNameToClassMap.get(thisProxyClassName);

            //Skipping non thisUser user table
            //Update: We may need related users for something esp if you are a non student.
            //          So we should not do this. Instead we should do checks on
            //          client side.
            //TODO: Remove this when all other devices are up to date
            if(thisProxyClass == User.class){
                User thisNewEntityUser = (User)thisNewEntity;
                if(!thisNewEntityUser.getUsername().equals(thisUser.getUsername())){
                    System.out.println("UMSync: jsonToDB: Skipping non User username. " +
                            "Remove me when all devices are up to date.");
                    continue;
                }
            }

            NanoLrsManagerSyncable thisManager = getManagerFromProxyClass(thisProxyClass);

            //Set entity's change seq from available pool (preSyncEntitySeqNumMap)
            long thisNewEntityNewSeq = preSyncEntitySeqNumMap.get(thisProxyClassName); //already the next seq num
            //long thisNewEntityNewSeq = thisEntityChangeSeq + 1; //this is the new seq num

            //Also update master to 0 if -1:
            if(thisNewEntity.getMasterSequence() < 0 ){
                thisNewEntity.setMasterSequence(0);
            }

            preSyncEntitySeqNumMap.put(thisProxyClassName, thisNewEntityNewSeq); //set the next one
            thisNewEntity.setLocalSequence(thisNewEntityNewSeq);
            //If master, update master sequence as well..
            if (thisNode != null) {
                if (thisNode.isMaster()) {
                    thisNewEntity.setMasterSequence(thisNewEntityNewSeq);
                }
            }

            ////////////////////////////////////
            ///  ENTITY CONFLICT RESOLUTION  ///
            ////////////////////////////////////
            boolean doIPersist =
                    shouldIPersistThisEntity(thisNewEntity, thisProxyClass, senderNode, thisNode, dbContext);

            //persist without doing a +1 to its change seq.
            //because we have already set it above (from pool)
            if(doIPersist) {
                //thisManager.persist(dbContext, thisNewEntity, false);
                System.out.println("jsonToDB(): Persisting entity's manager..");
                try {
                    Method persistMethod = thisManager.getClass().getMethod(
                            "persist", Object.class, NanoLrsModel.class, boolean.class);
                    persistMethod.invoke(thisManager, dbContext, (NanoLrsModel)thisNewEntity, false);
                    //thisManager.persist(dbContext, thisNewEntity, false);
                } catch (Exception e){
                    System.out.println("jsonToDB(): EXCEPTION Invoking Entity specific " +
                            "persist(). Running the general persist anyway..");
                    thisManager.persist(dbContext, thisNewEntity, false);
                }

                //System.out.println(" -> Persisting OK..");
                //+1 on the map
                preSyncEntitySeqNumMap.put(thisProxyClassName, thisNewEntityNewSeq+1);
            }

            ////////////////////////////////////
            ///     UPDATE SYNC STATUS       ///
            ////////////////////////////////////
            SyncStatus ss = (SyncStatus)
                    syncStatusManager.getSyncStatus(senderNode.getHost(),thisProxyClass, dbContext);
            long currentSent = ss.getSentSeq();
            if(thisNewEntityNewSeq > currentSent){
                ss.setSentSeq(thisNewEntityNewSeq);
                syncStatusManager.persist(dbContext, ss);
                //System.out.println("Sync Status updated OK..");
            }

            //TODO: PROXY: For Proxy: Update received?
            /*
            long currentRec = ss.getReceivedSeq();
            if(thisNewEntityNewSeq > currentRec) {
                ss.setReceivedSeq(thisNewEntityNewSeq);
                syncStatusManager.persist(dbContext, ss);
            }
            */
        }
        allgoood = true;
        return allgoood;
    }

    /**
     * Validates a stream if its a valid UM Sync Stream.
     *
     * @param inputStream InputStream to validate
     * @return UMSyncResult object with result status.
     * @throws UnsupportedEncodingException
     */
    public static UMSyncResult validateUMSyncStream(InputStream inputStream)
            throws UnsupportedEncodingException {
        String streamString;
        try {
            streamString = convertStreamToString2(inputStream, UTF_ENCODING);
            return validateUMSyncString(streamString);

        } catch (IOException e) {
            e.printStackTrace();
            //Cannot proceed. Stream is fauly. Skip?
            return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    /**
     * Validates a stream if its a valid UM Sync Stream.
     *
     * @param streamString  The stream string.
     * @return UMSyncresult object with result status.
     * @throws UnsupportedEncodingException
     */
    public static UMSyncResult validateUMSyncString(String streamString)
            throws UnsupportedEncodingException {
        JSONObject entitiesWithInfoJSON;
        try {
            if(streamString.isEmpty()){
                //Completely empty String.
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_NO_CONTENT);
            }
            if(!JsonUtil.isThisStringJSON(streamString)){
                //Not a valid JSON. What do we do ?
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
            }

            entitiesWithInfoJSON = new JSONObject(streamString);

            if(entitiesWithInfoJSON.optJSONArray(RESPONSE_ENTITIES_DATA) == null){
                //Valid JSON, Invalid UMSync JSON with no data or info. What do we do?
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            if(entitiesWithInfoJSON.optJSONArray(RESPONSE_ENTITIES_INFO) == null){
                //Valid JSON, Invalid UMSync JSON with no data or info. What do we do?
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
            }

        } catch (IOException e) {
            e.printStackTrace();
            //Cannot proceed. Stream is fauly. Skip?
            return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        return null;
    }

    /**
     * Checks if this username is available for a new user creation/conflict
     *  in incoming registration.
     *
     * @param username  The username to check.
     * @param dbContext The database context.
     * @return boolean true if username is available; false if not.
     */
    public static boolean isThisUsernameAvailable(String username, Object dbContext){
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        User userAlreadyExists = userManager.findByUsername(dbContext, username);
        if(userAlreadyExists == null){
            return true;
        }
        return false;
    }

    /**
     * Gets next available username.
     *
     * @param username  The username that is already taken.
     * @param dbContext The database context.
     * @return  String new username value.
     */
    public static String getNextAvailableUsername(String username, Object dbContext){

        if(isThisUsernameAvailable(username, dbContext)){
            return username;
        }
        int appendValue = (int)Math.floor(Math.random() * 101);
        String newUsername = username + appendValue;
        while(isThisUsernameAvailable(newUsername, dbContext)){
            if(isThisUsernameAvailable(newUsername, dbContext)){
                return newUsername;
            }
            appendValue++;
            newUsername = username + appendValue;
        }
        return null;
    }

    /**
     * Get header with oldHeader check. Gets a specific header from header map. Also checks old
     * header naming convention.
     * TODO: Phase out old header check after next few versions of the app.
     *
     * @param headers       Map of headers/
     * @param headerName    The header to check.
     * @return String The header value.
     */
    public static String getHeader(Map<String, String> headers, String headerName){
        //Enabling support for old header names.
        String oldHeaderName = null;
        if(headerName.startsWith("X-UM-")){
            oldHeaderName = headerName.substring("X-UM-".length(), headerName.length());
        }

        if(headers.get(headerName) == null && headers.get(headerName.toLowerCase()) == null){
            String value = headers.get(oldHeaderName);
            if(value == null){
                if(oldHeaderName != null &&
                        headers.get(oldHeaderName.toLowerCase()) != null){
                    value = headers.get(oldHeaderName.toLowerCase());
                }
            }
            return value;
        }

        //Checking lower case header name
        String val;
        val = headers.get(headerName);
        if(val==null){
            if(headers.get(headerName.toLowerCase()) != null){
                val = headers.get(headerName.toLowerCase());
            }
        }
        return val;
    }


    public static String[] getCredFromAuthString(String authorization){
        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = Base64CoderNanoLrs.decodeString(base64Credentials);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            return values;
        }
        return null;
    }

    public static String getPasswordFromBasicAuthString(String authorization){
        String[] credentials = getCredFromAuthString(authorization);
        if(credentials != null && credentials.length > 0) {
            return credentials[1];
        }
        return null;
    }


    /**
     * Handles incoming sync requests. Essentially an endpoint to process request and
     * update database and handle it.
     *
     * REMEMBER TO CALL updateSyncStatus() AFTER THIS METHOD !
     * @param inputStream   The request inputStream.
     * @param node This is the node that sent the sync request
     * @param headers   The request headers
     * @param parameters   The request parameters if any
     * @param dbContext     The database context
     * @return  SyncResult with status and any postSyncChangeSeq Map we need SyncStatus to update.
     */
    public static UMSyncResult handleIncomingSync(InputStream inputStream, Node node, Map headers,
                                                  Map parameters, Object dbContext)
            throws SQLException, IOException {

        /*
        /////////////////////
        //     STEPS       //
        /////////////////////
            1. Validate headers and param and input stream
            2. Get the json array from input stream
            3. Reserve a set of change sequence numbers for the incoming update from client
            4. convert to entities
            5. get number
            6. add to db (persist)
            7. Resolve conflicts (if any)
            7. Get updates for senderNode
            8. Send back any updates, conflicts, in the response body
            9. Add mapping to request so we can update sync status if result is OK
        */

        //The return result and status of the incoming request's sync on this node
        UMSyncResult resultResponse;
        int resultStatus;

        //Managers
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);

        JSONObject responseJSON = new JSONObject();
        JSONArray sendTheseEntitiesBack = new JSONArray();
        JSONArray sendTheseInfoBack = new JSONArray();
        JSONArray conflictEntitiesJSON = new JSONArray();
        boolean emptyResponse = true;

        //Map of <Entity object,entity proxy class name>
        //Makes it easier to determine what type of entity object this is
        // and easier to get its manager.
        Map<NanoLrsModelSyncable, String> allNewEntitiesMap = new HashMap<>();

        //Pre sync change seq for ALL syncable entities
        Map<Class, Long> preSyncAllEntitiesSeqMap = new HashMap<>();

        //List of entities that are conflicts
        // Added to this array so we can return it if cannot be resolved.
        List<NanoLrsModelSyncable> conflictEntries = new ArrayList<NanoLrsModelSyncable>();

        //Get this device/node
        Node thisNode = nodeManager.getThisNode(dbContext);

        //Get this user details
        String userUsername = getHeader(headers, HEADER_USER_USERNAME);
        String userPassword = getHeader(headers, HEADER_USER_PASSWORD);
        String userUUID = getHeader(headers, HEADER_USER_UUID);
        String isNew = getHeader(headers, HEADER_USER_IS_NEW);
        String justCreated = getHeader(headers, RESPONSE_USER_JUST_CREATED);
        if(justCreated == null){
            justCreated = "false";
        }
        User thisUser = null;

        //Get all syncable entities pre sync seq and put it in preSyncSeqMap
        preSyncAllEntitiesSeqMap = getAllEntitiesSeqNum(dbContext);

        ////////////////////////////////
        //     VALIDATE STREAM        //
        ////////////////////////////////
        String streamString;
        JSONObject entitiesWithInfoJSON;
        try {
            streamString = convertStreamToString2(inputStream, UTF_ENCODING);
            if(streamString.isEmpty()){
                //Completely empty String.
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_NO_CONTENT);
            }
            if(!JsonUtil.isThisStringJSON(streamString)){
                //Not a valid JSON. What do we do ?
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            //TODO: Use UMSyncData here
            entitiesWithInfoJSON = new JSONObject(streamString);

            if(entitiesWithInfoJSON.optJSONArray(RESPONSE_ENTITIES_DATA) == null){
                //Valid JSON, Invalid UMSync JSON with no data or info. What do we do?
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
            }
            if(entitiesWithInfoJSON.optJSONArray(RESPONSE_ENTITIES_INFO) == null){
                //Valid JSON, Invalid UMSync JSON with no data or info. What do we do?
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
            }

        } catch (IOException e) {
            e.printStackTrace();
            //Cannot proceed. Stream is faulty. Skip?
            return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
        }


        ////////////////////////////////
        //  SYNC COMPATIBILITY CHECK  //
        ////////////////////////////////
        //Sync compatibility check: Client<->Client
        if(!thisNode.isMaster() && !thisNode.isProxy()){
            System.out.println("\nSorry Client-Client not allowed.\n");
            return returnEmptyUMSyncResult(HttpURLConnection.HTTP_NOT_ACCEPTABLE);
        }
        //Sync compatibility check: Proxy<->[Client, Master]
        if(thisNode.isProxy()){
            System.out.println("\nProxy here. I don't have the new user syncing with me.\n" +
                    "Don't think I'm going to accept user from client. " +
                    "I'll wait till I sync with master instead.\n");
        }

        ////////////////////////////////
        //       VALIDATE USER        //
        ////////////////////////////////
        //Get Password from Basic Auth:
        String basicAuthCred = null;
        String basicAuthString = getHeader(headers, REQUEST_AUTHORIZATION);
        if(basicAuthString != null && !basicAuthString.isEmpty()){
            basicAuthCred = getPasswordFromBasicAuthString(basicAuthString);
            if(basicAuthCred != null && !basicAuthCred.isEmpty()){
                //If password in Basic Auth exists, we use that.
                userPassword = basicAuthCred;
            }
        }
        if(userUsername == null || userUsername.isEmpty() ||
                userPassword == null || userPassword.isEmpty()){
            System.out.println("UMSyncEndpoint: Username or Password is empty. BAD Request. " +
                    "Check startSync(). Might be a bug. Rejecting.");
            return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
        }

        if(isNew.equals("true")){

            //Check if username given in header is valid
            if(userUsername != null && !userUsername.isEmpty()){

                //Check if username already exists:
                User ifIExistChangeUsername = userManager.findByUsername(dbContext, userUsername);

                if(ifIExistChangeUsername != null){
                    if(justCreated.equals("true")){
                        System.out.println("User: (" + userUsername + ") just got created. " +
                                "Not checking for existing username. Already satisfied.");
                        thisUser = ifIExistChangeUsername;
                    }else {
                        //TODO: Check if we will ever oome to this code block.
                        System.out.println("\nUsername: (" + userUsername + ") already exists for new user.\n" +
                                "Changing it rejecting incoming sync with new username header.\n");
                        String newAvailableUsername = getNextAvailableUsername(userUsername, dbContext);
                        Map<String, String> changeYourUsernameHeader = new HashMap<>();
                        changeYourUsernameHeader.put(RESPONSE_CHANGE_USERNAME, newAvailableUsername);
                        return returnEmptyUMSyncResultWithHeader(
                                HttpURLConnection.HTTP_CONFLICT, changeYourUsernameHeader);
                    }

                }else {
                    //Username is new and valid, is available. Its a new user .
                    // So we make it..
                    if(userPassword != null && !userPassword.isEmpty()){
                        System.out.println("UMSyncEndpoint: handleIncomingSync: " +
                                "Username is valid and available. New user: (" + userUsername + ").");

                        thisUser = (User)userManager.makeNew();
                        thisUser.setUsername(userUsername);
                        if(userUUID != null && !userUUID.isEmpty()){
                            thisUser.setUuid(userUUID);
                        }else{
                            thisUser.setUuid(UUID.randomUUID().toString());
                        }

                        //The password in the header(old) and basic auth(new) is plain text.
                        // and it needs to be hashed.
                        try {
                            userPassword = userManager.hashPassword(userPassword);
                        } catch (NoSuchAlgorithmException e) {
                            System.out.println("Could not hash password for new user in " +
                                    "handleIncomingSync() : " + e);
                            e.printStackTrace();
                        }
                        thisUser.setPassword(userPassword);

                        /*
                        //This code block was +1 ing the change seq so that the user is an update.
                        // Not required anymore since we don't need to update it. The sync entities
                        // will most likely have it.

                        String userTableName = UMSyncEndpoint.getTableNameFromClass(User.class);
                        long newUserLocalSeq =
                                changeSeqManager.getNextChangeAddSeqByTableName(userTableName,
                                        1, dbContext);
                        thisUser.setLocalSequence(newUserLocalSeq);
                        */

                        //Persist without +1-ing change seq because for new entities
                        // because all details will come as part of this sync..
                        userManager.persist(dbContext, thisUser, false);
                    }else{
                        //No password given. BAD request
                        System.out.println("UMSyncEndpoint: handleIncomingSync(): " +
                                "No password given for new user ("+userUsername+"). BAD request.");
                        return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
                    }
                }
            }else{
                //No username value given. Have to null it. BAD request
                System.out.println("UMSyncEndpoint.handleIncomingSync(): No username given. BAD REQUEST!");
                return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
            }

        }else{
            //Not a new user.. find existing user
            thisUser = userManager.findByUsername(dbContext, userUsername);
            if(thisUser == null){
                //Existing User does not exist in the system.
                System.out.println("SYNCED USER DOES NOT EXIST HERE..");

                if(thisNode.isMaster()){
                    //Master. Odd since if master doesn't have it,
                    // and isNewUser is not true, it shouldn't even get to here.
                    // Maybe master deleted it (got refreshed).
                    //We should create it.
                    System.out.println("\nMaster here. I have a new user that's supposed to be with me,\n" +
                            " but i dont have it. I'll create it anyway.. \n");

                    if(userPassword != null && !userPassword.isEmpty()) {
                        thisUser = (User) userManager.makeNew();
                        thisUser.setUsername(userUsername);

                        //hash it. storing hashes from now on only.
                        try {
                            userPassword = userManager.hashPassword(userPassword);
                        } catch (NoSuchAlgorithmException e) {
                            System.out.println("Unable to hash password for master in " +
                                    "handleIncomingSync() " + e);
                            e.printStackTrace();
                        }

                        thisUser.setPassword(userPassword);
                        if(userUUID != null && !userUUID.isEmpty()){
                            thisUser.setUuid(userUUID);
                        }else{
                            thisUser.setUuid(UUID.randomUUID().toString());
                        }

                        userManager.persist(dbContext, thisUser, false);
                        //TODODone: If the user statement is coming, do we need to set a local seq? Check this logic
                        //Update: Very edge case. Can't put a breakpoint.Unlikely. However, if it doesn't exist, it will
                        // get created. However extra fields will not get synced unless there is an update on client.
                        // One way is to send back sendUserAgain request .. This will blank persist on User and UserField
                        // table so its an update over here..but it will update else where too.
                        // Or figure out to, force User and User fields in syncData upon next sync. Ignoring for now..
                        /*
                        Map<String, String> sendUserAgainHeader = new HashMap<>();
                        sendUserAgainHeader.put(RESPONSE_CHANGE_USERNAME, userUsername);
                        System.out.println("\n\nREQUESTING USER INFO AGAIN.GOT REMOVED HERE..\n\n");
                        return returnEmptyUMSyncResultWithHeader(
                                HttpURLConnection.HTTP_CONFLICT, sendUserAgainHeader);
                        */
                    }else{
                        System.out.println("UMSyncEndpoint.handleIncomingSync(): " +
                                "New user with null/empty password given.");
                    }
                }
                //TODO: Add for proxy/client
            }else{

                //Authenticate it..
                if(!userManager.authenticate(dbContext, userUsername, userPassword, true)){
                    //Not valid login.
                    if(userUsername == null){
                        userUsername = "";
                    }
                    System.out.println("UMSyncEndpoint.handleIncomingSync(): " +
                            "Sorry, Username (user:" + userUsername + ") and " +
                            "password does not match for sync");
                    return returnEmptyUMSyncResult(HttpURLConnection.HTTP_UNAUTHORIZED);
                }

            }
        }


        String thisNodeHost;
        try {
            thisNodeHost = thisNode.getHost();
        }catch (Exception e){
            thisNodeHost = "Not set";
        }

        String thisUsername = "null";
        if(thisUser != null){
            thisUsername = thisUser.getUsername();
        }
        System.out.println("UMSYNC: Incoming: Getting sync for user: " + thisUsername
                + " isNew?: " + isNew + " from Node:" + node.getHost()
                + " . I am Node: " + thisNodeHost);

        ////////////////////////////////
        //    INCOMING JSON TO DB     //
        ////////////////////////////////
        boolean allgood = jsonToDB(entitiesWithInfoJSON, node, thisNode, thisUser, dbContext);

        //Update thisUser if it was new, we set master to normal
        // so that we don't consider this as new no more.
        if(thisUser.getMasterSequence() < 0){
            Long localSeq = thisUser.getLocalSequence();
            thisUser.setMasterSequence(0);
            if(thisNode.isMaster()){
                if(localSeq != null && localSeq >0){
                    thisUser.setMasterSequence(localSeq);
                }
            }
            userManager.persist(dbContext, thisUser, false);
        }

        ////////////////////////////////
        // CONSTRUCT RETURN ENTITIES  //
        ////////////////////////////////
        //TODO: Use UMSyncData
        Map.Entry<JSONObject, Map<Class, Long>> returnEntitiesMap =
                getNewEntriesJSON(thisUser, node, null, preSyncAllEntitiesSeqMap, dbContext);
        sendTheseEntitiesBack = returnEntitiesMap.getKey().getJSONArray(RESPONSE_ENTITIES_DATA);
        sendTheseInfoBack = returnEntitiesMap.getKey().getJSONArray(RESPONSE_ENTITIES_INFO);

        Map<Class, Long> returnEntitiesChangeSeq = returnEntitiesMap.getValue(); //update sync status
        ////////////////////////////////////
        ///     UPDATE SYNC STATUS       ///
        ////////////////////////////////////
        /*
        Map<Class, Long> returnEntitiesChangeSeq = returnEntitiesMap.getValue(); //update sync status
        //TODODone: WE MAY NEED TO CHECK RESPONSE HEADERS IF ALL OKAY AND THEN UPDATE SS
        //Update: Moved to method and calling it in UMSyncServlet
        for (Map.Entry<Class, Long> thisEntityToLatestLocalSeqNumEntry : returnEntitiesChangeSeq.entrySet()) {
            SyncStatus ss = (SyncStatus)syncStatusManager.getSyncStatus(node.getHost(),
                    thisEntityToLatestLocalSeqNumEntry.getKey(), dbContext);
            long currentSentSeq = ss.getSentSeq();
            long latestSeqNumReturned = thisEntityToLatestLocalSeqNumEntry.getValue();
            //update only the latest
            if(latestSeqNumReturned > currentSentSeq){
                ss.setSentSeq(latestSeqNumReturned);
                syncStatusManager.persist(dbContext, ss);
            }
        }
        */

        ////////////////////////////////
        //   CONSTRUCT THE RESPONSE   //
        ////////////////////////////////
        Map<String, String> responseHeaders = createSyncHeader(thisUser, userPassword, node);
        if(allgood){
            System.out.println(" UMSync: Incoming: jsonToDB all good (for user:" +
                    thisUser.getUsername() + ").");
            resultStatus = HttpURLConnection.HTTP_OK;
            responseHeaders.put(RESPONSE_SYNCED_STATUS, RESPONSE_SYNC_OK);
        }else{
            resultStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            responseHeaders.put(RESPONSE_SYNCED_STATUS, RESPONSE_SYNC_FAIL);
        }
        //resultStatus = HttpURLConnection.HTTP_OK; //regardless of conflicts, its gonna be 200
        //responseHeaders.put(RESPONSE_SYNCED_STATUS, RESPONSE_SYNC_OK);
        String resultForClient;
        InputStream responseData;
        long responseLength;
        String emptyValidJSONString = "{\n" +
                "    \"data\": [],\n" +
                "    \"info\": []\n" +
                "}";

        if(conflictEntries != null && conflictEntries.size() > 0){
            //Add conflictEntries to response below
            for(NanoLrsModel thisConflictEntry:conflictEntries){
                JSONObject thisConflictEntryJSON =
                        ProxyJsonSerializer.toJson(thisConflictEntry, thisConflictEntry.getClass());
                conflictEntitiesJSON.put(thisConflictEntryJSON);
            }
        }
        if(conflictEntitiesJSON != null && conflictEntitiesJSON.length() > 0 ){
            responseJSON.put(RESPONSE_CONFLICT, conflictEntitiesJSON);
            emptyResponse = false;
        }
        if(sendTheseEntitiesBack != null && sendTheseEntitiesBack.length() > 0
                && sendTheseInfoBack != null && sendTheseInfoBack.length() >0){
            responseJSON.put(RESPONSE_ENTITIES_DATA, sendTheseEntitiesBack);
            responseJSON.put(RESPONSE_ENTITIES_INFO, sendTheseInfoBack);
            emptyResponse=false;
        }

        if(emptyResponse){
            responseData = new ByteArrayInputStream(emptyValidJSONString.getBytes(UTF_ENCODING));
            responseLength = emptyValidJSONString.length();

        }else{
            resultForClient = responseJSON.toString();
            responseData =
                    new ByteArrayInputStream(resultForClient.getBytes(UTF_ENCODING));
            responseLength = resultForClient.length();
        }

        long dataSize = entitiesWithInfoJSON.optJSONArray(RESPONSE_ENTITIES_DATA).length();
        resultResponse = new UMSyncResult(resultStatus,responseHeaders,
                responseData, responseLength, returnEntitiesChangeSeq, dataSize);

        return resultResponse;
    }

    /**
     * Method to update the sync status.
     *   eg: upon successful incoming sync on returned entities
     *
     * @param syncResult    The previous sync's result that has the entity<->ChangeSeq map
     * @param node          The node where the sync was made to
     * @param dbContext     Database context
     * @return              true if all good, false if not
     * @throws SQLException Since we are doing SQL stuff.
     */
    public static boolean updateSyncStatus(UMSyncResult syncResult, Node node, Object dbContext)
            throws SQLException {

        SyncStatusManager syncStatusManager =
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
        Map<Class, Long> returnEntitiesChangeSeq = syncResult.getPostSyncChangeSeqMap();
        boolean allgood = false;

        ////////////////////////////////////
        ///     UPDATE SYNC STATUS       ///
        ////////////////////////////////////
        for (Map.Entry<Class, Long> thisEntityToLatestLocalSeqNumEntry : returnEntitiesChangeSeq.entrySet()) {
            SyncStatus ss = (SyncStatus)syncStatusManager.getSyncStatus(node.getHost(),
                    thisEntityToLatestLocalSeqNumEntry.getKey(), dbContext);
            long currentSentSeq = ss.getSentSeq();
            long latestSeqNumReturned = thisEntityToLatestLocalSeqNumEntry.getValue();
            //update only the latest
            if(latestSeqNumReturned > currentSentSeq){
                ss.setSentSeq(latestSeqNumReturned);
                syncStatusManager.persist(dbContext, ss);
            }
        }
        allgood = true;
        return allgood;
    }

    public static UMSyncResult startSync(User thisUser, Node node, Object dbContext)
            throws SQLException, IOException {
        return startSync(thisUser, null, node, dbContext);
    }

    /**
     * Handles sync process : gets all entities to be synced from syncstatus seqnum and
     * builds entities list to convert to json array to send in a request to host's
     * syncURL endpoint. This should only be run if current node is set (thisNode).
     *
     * @param thisUser      The user starting the sync request
     * @param node          The node to with which we wish to sync
     * @param dbContext     The database context
     * @return              SyncResult Object
     * @throws SQLException because we are doing sql updates
     * @throws IOException  because of i/o exceptions
     */
    public static UMSyncResult startSync(User thisUser, String thisUserCred,  Node node,
                                         Object dbContext) throws SQLException, IOException {

        //Get managers
        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        NodeSyncStatusManager nodeSyncStatusManager =
                PersistenceManager.getInstance().getManager(NodeSyncStatusManager.class);

        //Get this device/node
        Node thisNode = nodeManager.getThisNode(dbContext);

        //Map of Entity and latestSeq got so we can update sync status upon sync success
        Map<Class, Long> entityToLatestLocalSeqNum = new HashMap<>();
        Map<Class, Long> entityToLatestMasterSeqNum = new HashMap<>();

        //Get sync headers and parameters
        Map<String, String> headers = createSyncHeader(thisUser, thisUserCred, thisNode);
        Map<String, String> parameters = createSyncParameters(thisUser, thisNode);

        //Create a Node<->NodeSyncStatus entry that this sync has started.
        NodeSyncStatus nodeSyncStatus = (NodeSyncStatus) nodeSyncStatusManager.makeNew();
        nodeSyncStatus.setUUID(UUID.randomUUID().toString());
        nodeSyncStatus.setNode(node);
        nodeSyncStatus.setHost(node.getHost());
        nodeSyncStatus.setSyncDate(System.currentTimeMillis());
        nodeSyncStatus.setSyncResult("");
        nodeSyncStatusManager.persist(dbContext, nodeSyncStatus);

        //Set thisUser master to 0 if its -1 (ie: its a new user (never synced with master)
        // so that we don't send that over
        boolean userWasNew = false;
        //Set not new for user so when we send the request, IS NEW header will not be sent
        if(thisUser.getMasterSequence() < 0){
            userWasNew = true;
            thisUser.setMasterSequence(0);
            //persist without updating its seq num
            userManager.persist(dbContext, thisUser, false);
        }

        //The sync JSON to send to node
        JSONObject pendingEntitiesWithInfo;

        Map.Entry<UMSyncData, Map<Class,Long>> syncInfo =
                getSyncInfo(thisUser, node, null, null, dbContext);
        pendingEntitiesWithInfo = syncInfo.getKey().toSyncJSON();
        entityToLatestLocalSeqNum = syncInfo.getValue();

        //Adding a header that sync was sent by a valid request.
        headers.put(RESPONSE_SYNCED_STATUS, RESPONSE_SYNC_OK);

        //Make a request with the JSON in POST body and return the UMSyncResult
        UMSyncResult syncResult = makeSyncRequest(node.getUrl(), "POST",
                thisUser.getUsername(), thisUserCred, headers, parameters, pendingEntitiesWithInfo,
                JSON_MIMETYPE, null);
        syncResult.setEntitiesCount(syncInfo.getKey().getEntities().size());
        Map responseHeaders = syncResult.getHeaders();

        if(syncResult.getStatus() == 200){
            //Check that its actually a request sent to a sync endpoint..
            //Maybe we want to enable trust esp against main node. TODO
            String syncStatusHeader = UMSyncEndpoint.getHeader(syncResult.getHeaders(),
                    RESPONSE_SYNCED_STATUS);
            if(syncStatusHeader != null && syncStatusHeader.equals(RESPONSE_SYNC_OK))
            {

                //Update the SyncStatus with latest value of seq num for this host and every entity
                Iterator<Map.Entry<Class, Long>> latestChangeSeqIterator =
                        entityToLatestLocalSeqNum.entrySet().iterator();
                while (latestChangeSeqIterator.hasNext()) {
                    Map.Entry<Class, Long> entityLatestChangeSeq =
                            latestChangeSeqIterator.next();

                    syncStatusManager.updateSyncStatusSeqNum(node.getHost(),
                            entityLatestChangeSeq.getKey(),
                            entityLatestChangeSeq.getValue(),
                            -1,
                            dbContext);
                }

                //TODO: PROXY: Implement PROXY LOGIC/Code: make entityToLatestMasterSeqNum & work with it.
                //This doesnt do anything right now..
                Iterator<Map.Entry<Class, Long>> latestMasterSeqIterator =
                        entityToLatestMasterSeqNum.entrySet().iterator();
                while(latestMasterSeqIterator.hasNext()){
                    Map.Entry<Class, Long> entityMasterSeqMap = latestMasterSeqIterator.next();
                    syncStatusManager.updateSyncStatusSeqNum(
                            node.getHost(),
                            entityMasterSeqMap.getKey(),
                            -1,
                            entityMasterSeqMap.getValue(),
                            dbContext);
                }
            }else{
                System.out.println("UMSyncEndpoint.statSync(): " +
                        "200 response but NO/FALSE RESPONSE_SYNCED_STATUS. IGNORING.");
                resetNewUser(thisUser, userWasNew, dbContext);
            }

        }else if(syncResult.getStatus() == 500){
            //Server busy, Something is up. If you want the endpoint to do something
            // here in addition to returning the object put it here..
            System.out.println("UMSyncEndpoint.startSync(): Sync SERVER ERROR: 500. IGNORING.");

            resetNewUser(thisUser, userWasNew, dbContext);

        }else if(syncResult.getStatus() == HttpURLConnection.HTTP_CONFLICT){
            //Server says there was a conflict with the request.
            System.out.println("UMSyncEndpoint.startSync(): Sync : CONFLICT..");
            if(responseHeaders == null){
                //fail.
                System.out.println("Username update for existence failed.");
                resetNewUser(thisUser, userWasNew, dbContext);
            }else{
                System.out.println("\nUMSyncEndpoint.startSync(): Sync: HTTP CONFLICT : " +
                        "RESPONSE HEADERS:\n" + responseHeaders);
                String newUsername = UMSyncEndpoint.getHeader(syncResult.getHeaders(),
                        RESPONSE_CHANGE_USERNAME);

                //Check: Conflict of username exists
                if (newUsername != null && !newUsername.isEmpty()) {
                    //Since its going to be a new one, set MS back to -1
                    // so that "is new" header will be set to true.
                    resetNewUser(thisUser, userWasNew, dbContext);

                    //Update username
                    userManager.updateUsername(newUsername, thisUser, dbContext);
                    //TODODone: Delete the old User (since it will still exist)
                    //Update: We just changed the user's username, thats all. Ignoring..
                }
            }
        }else if(syncResult.getStatus() == HttpURLConnection.HTTP_NOT_ACCEPTABLE ||
                syncResult.getStatus() == HttpURLConnection.HTTP_BAD_REQUEST ){
            //Server doesn't accept request as its either BAD (not complete) or Not acceptable
            System.out.println("UNSyncEndpoint.startSync(): " +
                    "Sync didn't happen because of BAD/NOT_ACCEPTABLE request.");
            resetNewUser(thisUser, userWasNew, dbContext);
        }else{
            //Default that sync did not happen.
            System.out.println("UMSyncEndpoint.startSync(): Sync didn't happen because of unknown status: "
                + syncResult.getStatus());
            resetNewUser(thisUser, userWasNew, dbContext);
        }

        //Check if response has conflicts, entities for us to process, etc
        JSONArray responseData = new JSONArray(); //response entities data
        JSONArray responseInfo = new JSONArray(); //response entities data info
        JSONArray conflictData = new JSONArray(); //response conflict data

        InputStream syncResultResponseStream = syncResult.getResponseData();
        String syncResultResponse = "";
        if(syncResultResponseStream != null) { //it will be null on a 404, etc
            syncResultResponse = convertStreamToString2(syncResultResponseStream, UTF_ENCODING);
        }
        if(!syncResultResponse.isEmpty()){
            //Get response entities and their info
            JSONObject syncResultAllResponseJSON = new JSONObject(syncResultResponse);
            if(syncResultAllResponseJSON != null){ //if we got something back..
                if(syncResultAllResponseJSON.optJSONArray(RESPONSE_ENTITIES_DATA) != null){
                    responseData = syncResultAllResponseJSON.getJSONArray(RESPONSE_ENTITIES_DATA);
                }
                if(syncResultAllResponseJSON.optJSONArray(RESPONSE_ENTITIES_INFO) != null){
                    responseInfo = syncResultAllResponseJSON.getJSONArray(RESPONSE_ENTITIES_INFO);
                }
                if(syncResultAllResponseJSON.optJSONArray(RESPONSE_CONFLICT) != null){
                    conflictData = syncResultAllResponseJSON.getJSONArray(RESPONSE_CONFLICT);
                }
            }

            ////////////////////////////////////
            ///      HANDLE CONFLICTS        ///
            ////////////////////////////////////
            if(conflictData != null){
                //TODODone: Handle Conflict and/or entities as they come back.
                //Update: We are handling conflicts on reception side.
                /*
                Handle Conflict:
                Step 1: Get JSON of conflict's data and info
                Step 2: Map both out for this process
                Step 3: Increment ChangeSeq for every entity to number of conflicts
                    for that entity
                Step 4: Measure for every entity, the changeSeq where it was at before
                    this conflict resolution
                Step 3: Loop through entities and increment localSequence to 1 from
                    the changeSeq above and ++ that for the next one.
                Step 4: Persist with changesEq boolean to false
                 */
                //...
                System.out.println("\n!!THERE WEERE CONFLICTS AFTER SERVER CHECK EVEN> " +
                        "\nPLEASE HANDLE THEM!\n");
            }

            ////////////////////////////////////
            ///   HANDLE RESPONSE ENTITIES   ///
            ////////////////////////////////////
            JSONObject responseDataInfo = new JSONObject();
            responseDataInfo.put(RESPONSE_ENTITIES_DATA, responseData);
            responseDataInfo.put(RESPONSE_ENTITIES_INFO, responseInfo);
            jsonToDB(responseDataInfo, node, thisNode, thisUser, dbContext);

        } //if response stream has something..end.

        nodeSyncStatus.setSyncDate(System.currentTimeMillis());
        nodeSyncStatus.setSyncResult(String.valueOf(syncResult.getStatus()));
        nodeSyncStatusManager.persist(dbContext, nodeSyncStatus);
        return syncResult;
    }

    /**
     * Resets new user's MS to -1 if userWasNew set to true. This method is often called when sync
     * fails and new users need to be reset for future syncs to work okay.
     *
     * @param thisUser
     * @param userWasNew
     * @param dbContext
     * @throws SQLException
     */
    public static void resetNewUser(User thisUser, boolean userWasNew, Object dbContext)
            throws SQLException {
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        if(userWasNew){
            System.out.println("UMSyncEndpoint.startSync(): Resetting new user.\n");
            thisUser.setMasterSequence(-1);
            //persist without updating its seq num
            userManager.persist(dbContext, thisUser, false);
        }

    }

    /**
     * Checks if this entity should be persisted. Check conflicts, if its a valid
     * update/new entity or if we should reject it.
     *
     * @param thisNewEntity     The entity (new/update) to be checked.
     * @param thisProxyClass    The entity's proxy class. eg: User.class
     * @param senderNode        The node that sent this entity (new/update)
     * @param thisNode          The node that accepted the entity.
     * @param dbContext         The database context.
     * @return boolean, true if persisting this entity OK. false if not (conflict, etc).
     * @throws SQLException
     */
    public static boolean shouldIPersistThisEntity(NanoLrsModelSyncable thisNewEntity,
                                   Class thisProxyClass, Node senderNode, Node thisNode,
                                                   Object dbContext) throws SQLException {

        SyncStatusManager syncStatusManager =
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
        boolean doIPersist = true;

        //Get manager
        NanoLrsManagerSyncable thisManager = getManagerFromProxyClass(thisProxyClass);
        //Get primary key field of this entity:
        String pkField = getPrimaryKeyFromClass(thisProxyClass);
        String pkMethodName = getPrimaryKeyMethodFromClass(thisProxyClass);

        //Get local version and check if its new or an update : Using reflection
        NanoLrsModelSyncable existingEntityToBeUpdated = null;
        try {
            Method pkMethod = thisProxyClass.getMethod(pkMethodName);
            existingEntityToBeUpdated = (NanoLrsModelSyncable)
                    thisManager.findByPrimaryKey(dbContext, pkMethod.invoke(thisNewEntity));
            System.out.println("    ENTITY UPDATE: " + pkMethod.invoke(thisNewEntity) + " (" + thisProxyClass.getSimpleName()+ ")");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        ////////////////////////////////////
        ///  UPDATE CONFLICT RESOLUTION  ///
        ////////////////////////////////////
        /*
           If conflict: array them which will be sent back in response
           If it is an update, check for conflicts
        */

        //Get sync status for this host and entity and its latest sent sync seq
        SyncStatus ss = (SyncStatus)
                syncStatusManager.getSyncStatus(senderNode.getHost(),thisProxyClass, dbContext);
        Long lastSyncSeq = syncStatusManager.getSentStatus(senderNode.getHost(),
                thisProxyClass, dbContext);

        if(existingEntityToBeUpdated != null){
            //Its an update
            long currentLatestMaster = existingEntityToBeUpdated.getMasterSequence();
            long thisNewEntityMaster = thisNewEntity.getMasterSequence();

            long currentStoredDate = existingEntityToBeUpdated.getStoredDate();
            long newStoredDate = thisNewEntity.getStoredDate();

            //Both have synced with master with no local changes.
            // Solution: get the latest master one
            if(currentLatestMaster > 0 && thisNewEntityMaster > 0){
                if(thisNewEntityMaster < currentLatestMaster){
                    //Skip: Already have a newer update from master
                    System.out.println("Already have this update from master. Skipping.");
                    doIPersist =false;
                }else if(thisNewEntityMaster == currentLatestMaster){
                    //We have an update that is from master and so is ours
                    //We also have both out masters the same. Which cannot be possible
                    //TODODone: Change this 0 to the the existing entity's stored Date
                    //Update: This was an old tudu. all good here..
                    System.out.println("Sync Conflict. Both entities have the same master.\n" +
                            "We cannot keep the update. We ignore this.\n");
                    doIPersist =false;
                }
            }
            //One of them has synced with master, the other has not : get the master
            //the other one has a more recent update but hasn't synced with master : favor master
            else if(currentLatestMaster > 0 || thisNewEntityMaster > 0){

                //One of them has an update and the other one is up to date with master
                //Possible rule : Master could always win
                if(currentLatestMaster > thisNewEntityMaster ){
                    System.out.println("Sync conflict resolution: " +
                            "Current Master is valid (not 0) and new master isn't.\n");

                    //Check dates between them
                    if(currentStoredDate > newStoredDate){
                        System.out.println(" .. but current is newer. skipping");
                        doIPersist =false;
                    }else{
                        System.out.println(" .. but incoming is newer. Allowing..");
                        doIPersist = true;
                    }

                }else{
                    //Accept the new entity because it has a higher master..
                }
            }
            else{ //None of them have a master sequence
                //neither of them have been synced with master.
                //Me and the sender could be clients or proxys

                //Case a: Sender is Proxy
                if(senderNode.isProxy()){
                    //The sender is a proxy and its got an update for an entry
                    //that has never been synced with master
                    //We gotta check if proxy's update is more recent than ours

                    //What if I am a proxy too:
                    if(thisNode.isProxy()){
                        //Proxys cannot talk right now.
                        System.out.println("\nIncoming Sync Conflict:" +
                                "Sender is Proxy and I am a proxy too.\n" +
                                "Not updating this entry.\n" +
                                "Two Proxy's cannot talk.");
                        doIPersist =false;
                    }
                    //What if I am master:
                    //If i was master, id have a master seq num

                    //What if I am a client:
                    //If I am a client:
                    else if(thisNewEntity.getLocalSequence() < lastSyncSeq){
                        System.out.println("\nIncoming Sync conflict:" +
                                "Sender is proxy.\n" +
                                "Sender's are already in the system. \n" +
                                "They shouldn't have been sent.\n" +
                                "Not updating this entry.\n");
                        doIPersist =false;

                    }else if(thisNewEntity.getLocalSequence() > lastSyncSeq){
                        //WAIT the sync wont come unless its greater than.
                        //Maybe we don't really even need this check..
                        //TODODone: check
                        //Update: Nope, not gonna come here.
                        System.out.println("\nIncoming Sync Resolution: " +
                                "Sender is From proxy\n" +
                                "Request from proxy: is higher. Accepting..\n" +
                                "\n\n!!!!THIS SHOULD NOT HAPPEN!!\n\n");
                        doIPersist = true;
                    }
                }
                //Case b: Sender is Master
                //If sender was master, it would have had a master sequence.
                //Rest is Case c:
                //Case c: Sender is Client
                else{
                    //Case c.1 : I am Master: INVALID
                    //Cannot be master as I don't have a master sequence.

                    //Case c.2 : I am Client: REJECT
                    if(!thisNode.isProxy() && !thisNode.isMaster()){
                        System.out.println("\nIncoming Sync conflict:" +
                                "Sender and I are both clients.\n" +
                                "Rejecting this.\n");
                        doIPersist =false;
                    }

                    //Case c.3 : I am Proxy:
                    //Sender is client, I am a proxy"
                    // We can accept the ones that are newer
                    else if(newStoredDate < currentStoredDate){
                        System.out.println("\nIncoming Sync resultion:" +
                                "Sender is a client\n" +
                                "Senders entries are not more recent.\n " +
                                "Not updating it (I have a newer version).\n"
                        );
                        doIPersist =false;
                    }
                }
            }

        }else{
            //Its a new entry, let it create it, there should be no conflicts
            //since this primary key does not exist on this node.
            doIPersist = true;
        }
        return doIPersist;
    }

}
