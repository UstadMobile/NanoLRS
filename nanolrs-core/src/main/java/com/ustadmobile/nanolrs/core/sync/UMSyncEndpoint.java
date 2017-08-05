package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiActivityManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.manager.XapiForwardingStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiStateManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.manager.XapiVerbManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.SyncStatus;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;
import com.ustadmobile.nanolrs.core.model.XapiActivity;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiState;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.model.XapiVerb;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.JsonUtil;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This sync endpoint is responsible for syncing databases between servers and other UstadMobile
 * instances. Sync is initiated on client and is communicated between other UstadMobile devices
 * and servers via HTTP request.
 * Created by varuna on 6/27/2017.
 */
public class UMSyncEndpoint {

    /**
     * Map of entity names to the proxy class
     */
    private static HashMap<String, Class> proxyNameToClassMap = new HashMap<>();
    private static HashMap<Class, Class> proxyClassToManagerMap = new HashMap<>();

    public static final String HEADER_NODE_NAME = "nodename";
    public static final String HEADER_NODE_HOST = "nodehost";
    public static final String HEADER_NODE_URL = "nodeurl";
    public static final String HEADER_NODE_UUID = "nodeuuid";
    public static final String HEADER_NODE_ROLE = "noderole";
    public static final String HEADER_NODE_ISMASTER = "nodeismaster";
    public static final String HEADER_NODE_ISPROXY = "nodeisproxy";

    public static final String HEADER_USER_USERNAME = "username";
    public static final String HEADER_USER_PASSWORD = "password";
    public static final String HEADER_USER_UUID = "useruuid";
    public static final String HEADER_USER_IS_NEW = "isnewuser";

    public static final String ENTITY_INFO_CLASS_NAME = "pCls";
    public static final String ENTITY_INFO_TABLE_NAME = "tableName";
    public static final String ENTITY_INFO_COUNT = "count";
    public static final String ENTITY_INFO_PRIMARY_KEY = "pk";

    public static final String RESPONSE_ENTITIES_DATA = "data";
    public static final String RESPONSE_ENTITIES_INFO = "info";
    public static final String RESPONSE_CONFLICT = "conflict";

    public static final String JSON_MIMETYPE = "application/json";

    public static final String REQUEST_CONTENT_LENGTH = "Content-Length";
    public static final String REQUEST_CONTENT_TYPE = "Content-Type";
    public static final String REQUEST_ACCEPT = "Accept";

    public static final String UTF_ENCODING = "UTF-8";

    //TODO: Find a central place for this and other mappings..
    static {
        proxyNameToClassMap.put(User.class.getName(), User.class);
        proxyClassToManagerMap.put(User.class, UserManager.class);

        proxyNameToClassMap.put(UserCustomFields.class.getName(), UserCustomFields.class);
        proxyClassToManagerMap.put(UserCustomFields.class, UserCustomFieldsManager.class);

        proxyNameToClassMap.put(XapiStatement.class.getName(), XapiStatement.class);
        proxyClassToManagerMap.put(XapiStatement.class, XapiStatementManager.class);

        proxyNameToClassMap.put(XapiActivity.class.getName(), XapiActivity.class);
        proxyClassToManagerMap.put(XapiActivity.class, XapiActivityManager.class);

        proxyNameToClassMap.put(XapiAgent.class.getName(), XapiAgent.class);
        proxyClassToManagerMap.put(XapiAgent.class, XapiAgentManager.class);

        //proxyNameToClassMap.put(XapiDocument.class.getName(),XapiDocument.class);
        //proxyClassToManagerMap.put(XapiDocument.class,XapiDocumentManager.class);

        proxyNameToClassMap.put(XapiForwardingStatement.class.getName(), XapiForwardingStatement.class);
        proxyClassToManagerMap.put(XapiForwardingStatement.class, XapiForwardingStatementManager.class);

        proxyNameToClassMap.put(XapiState.class.getName(), XapiState.class);
        proxyClassToManagerMap.put(XapiState.class, XapiStateManager.class);

        proxyNameToClassMap.put(XapiVerb.class.getName(), XapiVerb.class);
        proxyClassToManagerMap.put(XapiVerb.class, XapiVerbManager.class);
    }

    /**
     * Common method to return primary key when supplied a class entity
     *
     * @param syncableEntity eg: User.class
     * @return primary key field
     */
    public static String getPrimaryKeyFromClass(Class syncableEntity){
        Method[] allEntityMethods = syncableEntity.getMethods();
        String pkMethod = null;
        String pkField = null;
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
        System.out.println("!!!!DEBUG: pkField: " + pkField + " and pkMethod: " + pkMethod + "!!!!!");
        return pkField;
    }

    /**
     * Common method to return primary key when supplied a class entity
     *
     * @param syncableEntity eg: User.class
     * @return primary key field
     */
    public static String getPrimaryKeyMethodFromClass(Class syncableEntity){
        Method[] allEntityMethods = syncableEntity.getMethods();
        String pkMethod = null;
        String pkField = null;
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
        System.out.println("!!!!DEBUG: pkField: " + pkField + " and pkMethod: " + pkMethod + "!!!!!");
        return pkMethod;
    }

    /**
     * Common method to return manager from a proxy class
     * @param syncableEntity eg: User.class
     * @return
     */
    public static NanoLrsManagerSyncable getManagerFromProxyClass(Class syncableEntity){
        Class managerClass = proxyClassToManagerMap.get(syncableEntity);
        NanoLrsManagerSyncable syncableEntityManager = (NanoLrsManagerSyncable)
                PersistenceManager.getInstance().getManager(managerClass);
        return syncableEntityManager;
    }

    /**
     * Get manager from proxy class name
     * @param thisProxyClassName
     * @return
     */
    public static NanoLrsManagerSyncable getManagerFromProxyName(String thisProxyClassName){
        Class thisProxyClass = proxyNameToClassMap.get(thisProxyClassName);
        return getManagerFromProxyClass(thisProxyClass);
    }

    /**
     * Common method to return Info JSON about the entity for a given class
     * @param syncableEntity eg: User.class
     * @return
     */
    public static JSONObject createJSONFromClass(Class syncableEntity){
        JSONObject jsonInfo = new JSONObject();
        jsonInfo.put(ENTITY_INFO_CLASS_NAME, syncableEntity.getName());
        jsonInfo.put(ENTITY_INFO_TABLE_NAME, getTableNameFromClass(syncableEntity));
        jsonInfo.put(ENTITY_INFO_COUNT, 0);
        jsonInfo.put(ENTITY_INFO_PRIMARY_KEY, getPrimaryKeyFromClass(syncableEntity));

        return jsonInfo;
    }

    /**
     * Common method to return the table name from Proxy Class
     * @param syncableEntity eg: User.class
     * @return
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
     * Gets all syncable entities' next change seq number and stores it in a map against entity
     * @param dbContext
     * @return
     * @throws SQLException
     */
    public static Map<Class, Long> getAllEntitiesSeqNum(Object dbContext) throws SQLException {
        Map<Class, Long> allEntitiesSeqMap = new HashMap<>();
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);

        for(Class thisEntity:SYNCABLE_ENTITIES){
            //Pre-Sync : Add existing ChangeSeq value to preSyncAllEntitiesSeqMap
            String proxyClassName = thisEntity.getName();
            String tableName = getTableNameFromClass(thisEntity);
            long preSyncEntitySeqNum =
                    changeSeqManager.getNextChangeByTableName(tableName, dbContext);
            allEntitiesSeqMap.put(thisEntity, preSyncEntitySeqNum);
        }

        return allEntitiesSeqMap;
    }


    /**
     * Converts entitiesJSON to Entities mapped to their entity proxy class
     * @param entitiesJSON
     * @param dbContext
     * @return
     */
    public static Map<NanoLrsModelSyncable, String> entitiesJSONToEntitiesMap(
            JSONArray entitiesJSON, Object dbContext){
        Map<NanoLrsModelSyncable, String> allNewEntitiesMap = new HashMap<>();
        //Create Entity Map of <Entity Object, Proxy Class Name>
        for(int i=0; i < entitiesJSON.length(); i++){
            System.out.println(" -->JSON->Object");
            JSONObject entityJSON = entitiesJSON.getJSONObject(i);
            NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
            String thisProxyClass =
                    entityJSON.getString(ProxyJsonSerializer.PROXY_CLASS_JSON_FIELD);
            allNewEntitiesMap.put((NanoLrsModelSyncable)thisEntity, thisProxyClass);
            System.out.println("   ->OK.");
        }
        return allNewEntitiesMap;
    }

    /**
     * Update changeSeq with increment based on Entities info JSON, return preSync Map for every
     * entity.
     * @param entitiesInfoJSON
     * @param dbContext
     * @return
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
     * Returns an empty UMSyncResult with the given status code
     * @param resultStatus
     * @return
     * @throws UnsupportedEncodingException
     */
    public static UMSyncResult returnEmptyUMSyncResult(int resultStatus)
            throws UnsupportedEncodingException {
        String emptyResponseString = "";
        InputStream responseData = new ByteArrayInputStream(emptyResponseString.getBytes(UTF_ENCODING));
        long responseLength = 0;
        Map responseHeaders = new HashMap();

        UMSyncResult resultResponse = new UMSyncResult(resultStatus,responseHeaders,
                responseData, responseLength);

        return resultResponse;
    }

    /**
     * Converts a property name from e.g. from fullName to full_name
     *
     * @param propertyName Property Name e.g. propertyName
     *
     * @return Property named in lower case separated by underscores e.g. property_name
     */
    public static String convertCamelCaseNameToUnderscored(String propertyName) {
        String undererScoredName = "";
        for(int i = 0; i < propertyName.length(); i++) {
            if(Character.isUpperCase(propertyName.charAt(i)) && (i == 0 || Character.isLowerCase(propertyName.charAt(i-1)))) {
                undererScoredName += "_";
            }
            undererScoredName += Character.toLowerCase(propertyName.charAt(i));
        }

        return undererScoredName;
    }

    /**
     * Creates a JOSNInfo Object from class with count
     * @param syncableEntity
     * @param count
     * @return
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
     * Gets the latest seqNum for an array of one entitytyped array list
     * @param pendingEntitesToBeSynced
     * @return
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
     * @param pendingEntitesToBeSynced
     * @param syncableEntity
     * @return
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
        //return entitiesDataInfoMap;
    }

    /**
     * Creates headers needed for sync request
     * @param user
     * @param node
     * @return
     */
    public static Map <String, String> createSyncHeader(User user, Node node){
        //Headers if any..
        Map <String, String> headers = new HashMap<String, String>();
        headers.put(HEADER_USER_USERNAME, user.getUsername());
        headers.put(HEADER_USER_PASSWORD, user.getPassword());
        headers.put(HEADER_USER_UUID, user.getUuid());
        String isNewUser = "false";
        if(user.getMasterSequence() < 1 ) {
            isNewUser = "true";
        }
        headers.put(HEADER_USER_IS_NEW, isNewUser);

        headers.put(HEADER_NODE_UUID, node.getUUID());
        headers.put(HEADER_NODE_HOST, node.getHost());
        headers.put(HEADER_NODE_URL, node.getUrl());
        //mostly its "client" as they are the ones that start sync.
        //However that could change, so sending role.
        //TODO: we need to validate these roles somehow
        //mayb: tokens that get authorised like certificates.
        String thisNodeRole="client";
        if(node.isMaster()){
            thisNodeRole = "master";
        }
        if(node.isProxy()){
            thisNodeRole = "proxy";
        }
        headers.put(HEADER_NODE_ROLE, thisNodeRole);

        return headers;

    }

    /**
     * Creates parameters needed for sync request (doesnt do anything now)
     * @param user
     * @param node
     * @return
     */
    public static Map<String, String> createSyncParameters(User user, Node node){
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("someparameter", "somevalue");
        return parameters;
    }

    /**
     * Store syncable entities.
     * TODO: Put this in one common place , OR
     * TODO: Find a way to get all from NanoLrsModelSyncable extentsion.
     */
    public static Class[] SYNCABLE_ENTITIES = new Class[]{
            User.class, XapiStatement.class, XapiActivity.class, XapiAgent.class,
            //XapiDocument.class, XapiForwardingState.class  //Disabled: not needed?
            XapiState.class, XapiVerb.class, UserCustomFields.class
    };

    /**
     * Sets headers to the connection from a given header Map
     * @param connection
     * @param headers
     * @throws IOException
     */
    private static void setHeaders(HttpURLConnection connection, Map headers) throws IOException {
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            connection.setRequestProperty(pair.getKey().toString(), pair.getValue().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    /**
     * Find all pending JSON needed to be synced for all entities at this moment, or since the
     * given changeSeq numbers map.
     * @param thisUser
     * @param node
     * @param dbContext
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static  Map.Entry<JSONObject, Map<Class, Long>>getNewEntriesJSON (User thisUser,
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
        for(Class syncableEntity : SYNCABLE_ENTITIES) {
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
            List<NanoLrsModel> pendingEntitesToBeSynced=
                    syncableEntityManager.getAllSinceTwoSequenceNumber(thisUser, node.getHost(),
                            fromSyncSeq, toSyncSeq, dbContext);

            /*List<NanoLrsModel> pendingEntitesToBeSynced =
                    syncableEntityManager.getAllSinceSequenceNumber(
                            thisUser, dbContext, node.getHost(), lastSyncSeq);*/

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
     * Makes Sync Request with given JSON, headers, etc. Returns a UMSyncResult object
     * @param destURL
     * @param method
     * @param headers
     * @param parameters
     * @param dataJSON
     * @param contentType
     * @param content
     * @return
     */
    public static UMSyncResult makeSyncRequest(String destURL, String method, Map headers,
                                               Map parameters, JSONObject dataJSON, String contentType, byte[] content) {
        UMSyncResult response = new UMSyncResult();

        HttpURLConnection con = null;
        OutputStream out = null;
        OutputStreamWriter outw = null;
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

            if(!dataJSON.equals(null) && dataJSON.length()>0 && content == null){
                int cl = dataJSON.toString().getBytes().length;
                con.setFixedLengthStreamingMode(cl);
                //con.setFixedLengthStreamingMode(dataJSON.toString().getBytes().length());

                outw = new OutputStreamWriter(con.getOutputStream());
                outw.write(dataJSON.toString());
                outw.flush();
            }else if(content != null){
                con.setFixedLengthStreamingMode(content.length);
                out = con.getOutputStream();
                out.write(content);
                out.flush();
                out.close();
                out = null;
            }else if(parameters != null && method.equalsIgnoreCase("POST")){
                //Build String from param map
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
                byte[] postData = paramString.getBytes(UTF_ENCODING);
                int postDataLength = postData.length;
                con.setRequestProperty( REQUEST_CONTENT_LENGTH, Integer.toString( postDataLength ));
                //con.setUseCaches( false );

                outw = new OutputStreamWriter(con.getOutputStream());
                outw.write(paramString);
                outw.flush();
            }

            int statusCode = con.getResponseCode();
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
            if(outw != null){
                try{ outw.close();}
                catch(IOException ioe){}
            }
            if(con != null) {
                //con.disconnect();
                //TODO: Check, disabled because it makes InputStream invalid
            }
        }

        return response;
    }

    /**
     * Checks, updates, creats persists all JSON in given to database for a sender and receiver node.
     * @param entitiesWithInfoJSON
     * @param senderNode
     * @param thisNode
     * @param dbContext
     * @throws SQLException
     */
    public static void jsonToDB(JSONObject entitiesWithInfoJSON, Node senderNode, Node thisNode, Object dbContext)
            throws SQLException {
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
            NanoLrsModelSyncable thisNewEntity = thisNewEntityMap.getKey();
            String thisProxyClassName = thisNewEntityMap.getValue();
            Class thisProxyClass = proxyNameToClassMap.get(thisProxyClassName);
            NanoLrsManagerSyncable thisManager = getManagerFromProxyClass(thisProxyClass);

            //Set entity's change seq from available pool (preSyncEntitySeqNumMap)
            long thisNewEntityNewSeq = preSyncEntitySeqNumMap.get(thisProxyClassName); //already the next seq num
            //long thisNewEntityNewSeq = thisEntityChangeSeq + 1; //this is the new seq num

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
                thisManager.persist(dbContext, thisNewEntity, false);
                System.out.println(" -> Persisting OK..");
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
                System.out.println("Sync Status updated OK..");
            }

            //TODO: For Proxy: Update received?
            /*
            long currentRec = ss.getReceivedSeq();
            if(thisNewEntityNewSeq > currentRec) {
                ss.setReceivedSeq(thisNewEntityNewSeq);
                syncStatusManager.persist(dbContext, ss);
            }
            */
        }
    }

    /**
     * Validates a stream if its a valid UM Sync Stream
     * @param inputStream
     * @return
     * @throws UnsupportedEncodingException
     */
    public static UMSyncResult validateUMSyncStream(InputStream inputStream)
            throws UnsupportedEncodingException {
        String streamString;
        JSONObject entitiesWithInfoJSON;
        try {
            streamString = convertStreamToString2(inputStream, UTF_ENCODING);
            return validateUMSyncSring(streamString);

        } catch (IOException e) {
            e.printStackTrace();
            //Cannot proceed. Stream is fauly. Skip?
            return returnEmptyUMSyncResult(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    /**
     * Validates a stream if its a valid UM Sync Stream
     * @param streamString
     * @return
     * @throws UnsupportedEncodingException
     */
    public static UMSyncResult validateUMSyncSring(String streamString)
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
     * Handles incoming sync requests. Essentially an endpoint to process request and
     * update database and handle it
     * @param inputStream
     * @param node This is the node that sent the sync request
     * @param headers
     * @return
     */
    public static UMSyncResult handleIncomingSync(InputStream inputStream, Node node, Map headers,
                                                  Map parameters, Object dbContext)
            throws SQLException, IOException {

        /*
        Steps:
        1. Validate headers and param and input stream
        2. Get the json array from input stream
        3. Reserve a set of change sequence numbers for the incoming update from client
        4. convert to entities
        5. get number
        6. add to db (persist)
        7. Resolve conflicts (if any)
        7. Get updates for senderNode
        8. Send back any updates, conflicts, in the response body
        9. Update SyncStatus table
        */

        //The return result and status of the incoming request's sync on this node
        UMSyncResult resultResponse;
        int resultStatus;

        //Managers
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        SyncStatusManager syncStatusManager =
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
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

        //Get this user
        String userUuid = headers.get(HEADER_USER_UUID).toString();
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

        ////////////////////////////////
        //    INCOMING JSON TO DB     //
        ////////////////////////////////
        jsonToDB(entitiesWithInfoJSON, node, thisNode, dbContext);

        ////////////////////////////////
        // CONSTRUCT RETURN ENTITIES  //
        ////////////////////////////////
        thisUser = userManager.findById(dbContext,userUuid); //Get the user(it might have synced now)
        //TODO: Check if User null, headers will fail to be set..
        Map.Entry<JSONObject, Map<Class, Long>> returnEntitiesMap =
                getNewEntriesJSON(thisUser, node, null, preSyncAllEntitiesSeqMap, dbContext);
        sendTheseEntitiesBack = returnEntitiesMap.getKey().getJSONArray(RESPONSE_ENTITIES_DATA);
        sendTheseInfoBack = returnEntitiesMap.getKey().getJSONArray(RESPONSE_ENTITIES_INFO);

        ////////////////////////////////////
        ///     UPDATE SYNC STATUS       ///
        ////////////////////////////////////
        Map<Class, Long> returnEntitiesChangeSeq = returnEntitiesMap.getValue(); //update sync status
        //TODO: WE MAY NEED TO CHECK RESPONSE HEADERS IF ALL OKAY AND THEN UPDATE SS
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

        ////////////////////////////////
        //   CONSTRUCT THE RESPONSE   //
        ////////////////////////////////
        resultStatus = 200; //regardless of conflicts, its gonna be 200
        Map<String, String> responseHeaders = createSyncHeader(thisUser, node);
        String resultForClient;
        InputStream responseData;
        long responseLength;

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
            String emptyResponseString = "";
            responseData = new ByteArrayInputStream(emptyResponseString.getBytes(UTF_ENCODING));
            responseLength = 0;
        }else{
            resultForClient = responseJSON.toString();
            responseData =
                    new ByteArrayInputStream(resultForClient.getBytes(UTF_ENCODING));
            responseLength = resultForClient.length();
        }

        resultResponse = new UMSyncResult(resultStatus,responseHeaders,
                responseData, responseLength);

        return resultResponse;
    }

    /**
     * Handles sync process : gets all entites to be synced from syncstatus seqnum and
     * builds entities list to convert to json array to send in a request to host's
     * syncURL endpoint
     * @param node : The server, client, proxy, etc
     * @return
     */
    public static UMSyncResult startSync(User thisUser, Node node, Object dbContext)
            throws SQLException, IOException {
        /*
        Steps:
        1. We check the syncURL < make sure its a valid url
        2. We check the host and see if we have it stored in SyncStatus table
        3. If we don't have the host, we assume this is first time sync
        4. We get all Syncable entites
        5. Loop over every entity: For this entity get seq number from SyncStatus
        6. Get all entities list for that entity that remain to be synced
        7. Convert those entities list to JSON array
        8. Make a request
        9. Send >>
         */

        //Get managers
        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);

        //Get this device/node
        Node thisNode = nodeManager.getThisNode(dbContext);

        //Map of Entity and latestSeq got so we can update sync status upon sync success
        Map<Class, Long> entityToLatestLocalSeqNum = new HashMap<>();
        Map<Class, Long> entityToLatestMasterSeqNum = new HashMap<>();

        //The JSON to send back
        JSONObject pendingEntitiesWithInfo;

        //Get all entities since now into a JSON and get every entity type's
        // last change seq number
        Map.Entry<JSONObject, Map<Class, Long>> entitiesJSONAndChangeSeqMap =
                getNewEntriesJSON(thisUser, node, null, null, dbContext);
        entityToLatestLocalSeqNum = entitiesJSONAndChangeSeqMap.getValue();
        pendingEntitiesWithInfo = entitiesJSONAndChangeSeqMap.getKey();

        //Get sync headers and parameters
        Map<String, String> headers = createSyncHeader(thisUser, thisNode);
        Map<String, String> parameters = createSyncParameters(thisUser, thisNode);

        //Make a request with the JOSN in POST body and return the
        //UMSyncResult
        UMSyncResult syncResult = makeSyncRequest(node.getUrl(), "POST", headers, parameters,
                pendingEntitiesWithInfo, JSON_MIMETYPE, null );

        //Update the SyncStatus with latest value of seq num for
        // this host and every entity
        if(syncResult.getStatus() == 200){

            Map responseHeaders = syncResult.getHeaders();
            //TODO: Read response headers and check that all synced OK and that
            //this is not a 200 for any random page.

            Iterator<Map.Entry<Class, Long>> entityToLatestLocalSeqNumIterator =
                    entityToLatestLocalSeqNum.entrySet().iterator();
            while(entityToLatestLocalSeqNumIterator.hasNext()){
                Map.Entry<Class, Long> thisEntityToLatestLocalSeqNumEntry =
                        entityToLatestLocalSeqNumIterator.next();
                syncStatusManager.updateSyncStatusSeqNum(node.getHost(),
                        thisEntityToLatestLocalSeqNumEntry.getKey(),
                        thisEntityToLatestLocalSeqNumEntry.getValue(),
                        -1,
                        dbContext);
            }

            //TODO: Check for proxy
            Iterator<Map.Entry<Class, Long>> entityToLatestMasterSeqNumIterator =
                    entityToLatestMasterSeqNum.entrySet().iterator();
            while(entityToLatestMasterSeqNumIterator.hasNext()){
                Map.Entry<Class, Long> thisEntityToLatestMasterSeqNum = entityToLatestMasterSeqNumIterator.next();
                syncStatusManager.updateSyncStatusSeqNum(node.getHost(),
                        thisEntityToLatestMasterSeqNum.getKey(),
                        -1,
                        thisEntityToLatestMasterSeqNum.getValue(),
                        dbContext);
            }
        }else if(syncResult.getStatus() == 500){
            //Server busy, Something is up. If you want the endpoint
            // to do something here in addition to returning the object
            // put it here..
        }

        //Check if response has conflicts, entities for us to process, etc
        JSONArray responseData = new JSONArray(); //response entities data
        JSONArray responseInfo = new JSONArray(); //response entities data info

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
            }
            //Get sync conflict data
            JSONObject syncResultConflictJSON = null;
            if(syncResultAllResponseJSON.optJSONObject("data") != null){
                if(syncResultAllResponseJSON.optJSONObject("data").optJSONObject("data") != null){
                    if(syncResultAllResponseJSON.getJSONObject("data").getJSONObject("data").has(RESPONSE_CONFLICT)){
                        syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("data").getJSONObject("data").getJSONObject(RESPONSE_CONFLICT);
                    }
                }else{
                    if(syncResultAllResponseJSON.optJSONObject("data").has(RESPONSE_CONFLICT)){
                        syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("data").getJSONObject(RESPONSE_CONFLICT);
                    }
                }
            }
            if(syncResultAllResponseJSON.optJSONObject("json")!=null){
                if(syncResultAllResponseJSON.getJSONObject("json").optJSONObject("data") != null){
                    if(syncResultAllResponseJSON.getJSONObject("json").getJSONObject("data").has(RESPONSE_CONFLICT)){
                        syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("json").getJSONObject("data").getJSONObject(RESPONSE_CONFLICT);
                    }
                }
                if(syncResultAllResponseJSON.getJSONObject("json").has(RESPONSE_CONFLICT)){
                    syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("json").getJSONObject(RESPONSE_CONFLICT);
                }
            }

            ////////////////////////////////////
            ///      HANDLE CONFLICTS        ///
            ////////////////////////////////////
            if(syncResultConflictJSON != null){
                //TODO: Handle Conflict and/or entities as they come back.
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
            }

            ////////////////////////////////////
            ///   HANDLE RESPONSE ENTITIES   ///
            ////////////////////////////////////
            JSONObject responseDataInfo = new JSONObject();
            responseDataInfo.put(RESPONSE_ENTITIES_DATA, responseData);
            responseDataInfo.put(RESPONSE_ENTITIES_INFO, responseInfo);
            jsonToDB(responseDataInfo, node, thisNode, dbContext);
            /*
            //Reserve set of ChangeSeq numbers for every entity type
            //Increment every Entity's ChangeSeq by count of new updates
            Map<String, Long> preSyncEntitySeqNumMap =
                    getEntityChangeSeqAndIncrementItForInfo(responseInfo,dbContext);
            Map<NanoLrsModelSyncable, String> allNewEntitiesMap = new HashMap<>();

            if(!responseData.isNull(0)){
                for(int i=0;i<responseData.length();i++){
                    JSONObject entityJSON = responseData.getJSONObject(i);
                    NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
                    String thisProxyClassName =
                            entityJSON.getString(ProxyJsonSerializer.PROXY_CLASS_JSON_FIELD);
                    allNewEntitiesMap.put((NanoLrsModelSyncable)thisEntity, thisProxyClassName);

                    Class thisProxyClass = proxyNameToClassMap.get(thisProxyClassName);
                    NanoLrsManager thisManager = getManagerFromProxyClass(thisProxyClass);

                    SyncStatus ss = (SyncStatus)syncStatusManager.getSyncStatus(node.getHost(),
                            thisProxyClass, dbContext);

                    //Set entity's change seq from available pool (preSyncEntitySeqNumMap)
                    long thisEntityChangeSeq = preSyncEntitySeqNumMap.get(thisProxyClassName);
                    long thisNewEntityNewSeq = thisEntityChangeSeq + 1; //this is the new seq num
                    preSyncEntitySeqNumMap.put(thisProxyClassName, thisNewEntityNewSeq); //set the next one
                    ((NanoLrsModelSyncable) thisEntity).setLocalSequence(thisNewEntityNewSeq);

                    thisManager.persist(dbContext, thisEntity);

                    //Update SS's sent if thisEntity's value is greater
                    if(thisNewEntityNewSeq > ss.getSentSeq()){
                        ss.setSentSeq(thisNewEntityNewSeq);
                        syncStatusManager.persist(dbContext, ss);
                    }
                }
            }

            if(!responseInfo.isNull(0)){

            }
            */

        } //if response stream has something..end.

        return syncResult;
    }

    /**
     * Checks if this entity should be persisted. Check conflicts, if its a valid
     * update/new entity or if we should reject it.
     * @param thisNewEntity
     * @param thisProxyClass
     * @param senderNode
     * @param thisNode
     * @param dbContext
     * @return
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
            System.out.println("!!!!!DEBUG:GETTING PK METHOD!!!!!");
            Method pkMethod = thisProxyClass.getMethod(pkMethodName);
            System.out.println("!!!!DEBUG: PK METHOD:" + pkMethod +"!!!!!");
            existingEntityToBeUpdated = (NanoLrsModelSyncable)
                    thisManager.findByPrimaryKey(dbContext, pkMethod.invoke(thisNewEntity));
            System.out.println("ENTITY UPDATE: " + pkMethod.invoke(thisNewEntity));
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
                    //TODO: Change this 0 to the the existing entity's stored Date
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
                        //TODO: check
                        System.out.println("\nIncoming Sync Resolution: " +
                                "Sender is From proxy\n" +
                                "Request from proxy: is higher. Accepting..\n");
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
