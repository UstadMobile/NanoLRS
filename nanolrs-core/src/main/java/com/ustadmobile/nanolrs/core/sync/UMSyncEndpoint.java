package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.ThisNodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.ThisNode;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

    //TODO: Find a central place for this and other mappings..
    static {
        proxyNameToClassMap.put(User.class.getName(), User.class);
        proxyClassToManagerMap.put(User.class, UserManager.class);
    }

    public static String convertStreamToString(InputStream is, String encoding) {
        java.util.Scanner s = new java.util.Scanner(is, encoding).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
            throws SQLException, UnsupportedEncodingException {

        /*
        Steps:
        1. Validate headers and param and input stream
        2. Get the json array from input stream
        3. Reserve a set of change sequence numbers for the incoming update from client
        4. convert to entities
        5. get number
        6. add to db (persist)
        7. Send response
        */

        int resultStatus;

        //Managers
        UMSyncResult resultResponse = null;
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);
        ThisNodeManager thisNodeManager =
                PersistenceManager.getInstance().getManager(ThisNodeManager.class);
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);

        //Map of <entity object,entity class name>
        Map<NanoLrsModelSyncable, String> allNewEntitiesMap =
                new HashMap<NanoLrsModelSyncable, String>();
        //Map of <entity class name, current Seq number before sync>
        Map<String, Long> preSyncEntitySeqNumMap = new HashMap<>();

        //List of entities that are conflicts - Added to this array so we can return it, etc
        List<NanoLrsModelSyncable> conflictEntries = new ArrayList<NanoLrsModelSyncable>();

        //Convert inputstream->string->entities json array
        String streamString = convertStreamToString(inputStream, "UTF-8");
        JSONObject entitiesWithInfoJSON = new JSONObject(streamString);
        JSONArray entitiesJSON = entitiesWithInfoJSON.getJSONArray("data");
        JSONArray entitiesInfoJSON = entitiesWithInfoJSON.getJSONArray("info");

        //Create Entity Object, Proxy Class Name Map for easier manager lookup
        //Loop over JSON Array to put every JSON and its name in a MAP
        for(int i=0; i < entitiesJSON.length(); i++){
            JSONObject entityJSON = entitiesJSON.getJSONObject(i);
            NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
            String thisProxyClass =
                    entityJSON.getString(ProxyJsonSerializer.PROXY_CLASS_JSON_FIELD);
            allNewEntitiesMap.put((NanoLrsModelSyncable)thisEntity, thisProxyClass);
        }

        //Reserve set of ChangeSeq numbers for every entity type
        //Store previous changeseq in a map that we can +1
        if (entitiesInfoJSON.length() > 0 ){
            //for every entity type ..
            for(int j=0;j<entitiesInfoJSON.length();j++){
                JSONObject thisEntityInfoJSON = entitiesInfoJSON.getJSONObject(j);
                String proxyClassName = thisEntityInfoJSON.getString("pCls");
                String tableName = thisEntityInfoJSON.getString("tableName");
                int count = thisEntityInfoJSON.getInt("count");
                //get current change seq number locally..
                long preSyncEntitySeqNum =
                        changeSeqManager.getNextChangeByTableName(tableName, dbContext);
                //Add this to a map to be used later
                preSyncEntitySeqNumMap.put(proxyClassName, preSyncEntitySeqNum);
                //Increment the table with the count of new, updated entities so newer ones
                //get added after this increment.
                changeSeqManager.getNextChangeAddSeqByTableName(tableName, count, dbContext);
            }
        }

        //Get this device/node
        //Needs to get created if not set by the device itself. Name can be a combination
        // of device name, location, random uuid.toString(), etc.
        //Primary key needs to be set to "this_device"
        //We use that to check if this device is the master server or not..
        //Also needs some form of authentication, else - anyone can  be master
        //ThisNode thisNode = (ThisNode) thisNodeManager.findByPrimaryKey(dbContext, "this_device");
        Node thisNode = (Node)nodeManager.findByPrimaryKey(dbContext, "this_node");

        //Loop over the <Entities, pCls> to add them to this node's DB and persist
        Iterator<Map.Entry<NanoLrsModelSyncable, String>> allNewEntitiesMapIterator =
                allNewEntitiesMap.entrySet().iterator();
        while(allNewEntitiesMapIterator.hasNext()){
            Map.Entry<NanoLrsModelSyncable, String> thisNewEntityMap = (Map.Entry)
                    allNewEntitiesMapIterator.next();
            NanoLrsModelSyncable thisNewEntity = thisNewEntityMap.getKey();
            String thisProxyClassName = thisNewEntityMap.getValue();

            //Get manager
            Class thisProxyClass = proxyNameToClassMap.get(thisProxyClassName);
            Class thisManagerClass = proxyClassToManagerMap.get(thisProxyClass);
            NanoLrsManagerSyncable thisManager = (NanoLrsManagerSyncable)
                    PersistenceManager.getInstance().getManager(thisManagerClass);

            //Get entity's change seq before increment
            long thisEntityChangeSeq = preSyncEntitySeqNumMap.get(thisProxyClassName);
            long thisNewEntitySeq = thisEntityChangeSeq + 1;
            preSyncEntitySeqNumMap.put(thisProxyClassName, thisNewEntitySeq);
            thisNewEntity.setLocalSequence(thisNewEntitySeq);

            //If master, update master sequence as well..
            if (thisNode != null) {
                if (thisNode.isMaster()) {
                    thisNewEntity.setMasterSequence(thisNewEntitySeq);
                }
            }

            /* Conflict resolution:
            Check if thisNewEntity if present (by pk) is an update ie:
                a. Has a new Master Seq
                b. Has a Master Seq > Current Master Seq
                c. Has the same master seq, different local seq but
                is modified later
                d. .. (can you think of any more cases?)

             If conflict: array them which will be sent back in response
             */
            //Get local version of the update
            //Update: Using reflection
            String pkField = null;
            for(int k=0;k<entitiesInfoJSON.length();k++){
                JSONObject eInfoJSON = entitiesInfoJSON.getJSONObject(k);
                if(eInfoJSON.getString("pCls").equals(thisProxyClassName)){
                    pkField = eInfoJSON.getString("pk");
                    if (!pkField.isEmpty()){
                        pkField = "get" + Character.toUpperCase(pkField.charAt(0))
                                + pkField.substring(1);
                    }
                    break;
                }
            }
            NanoLrsModelSyncable existingEntityToBeUpdated = null;
            try {
                Method pkMethod = thisProxyClass.getMethod(pkField);
                existingEntityToBeUpdated = (NanoLrsModelSyncable)
                        thisManager.findByPrimaryKey(dbContext, pkMethod.invoke(thisNewEntity));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if(existingEntityToBeUpdated != null){
                //Its an update
                long currentLatestMaster = existingEntityToBeUpdated.getMasterSequence();
                long thisNewEntityMaster = thisNewEntity.getMasterSequence();
                if(currentLatestMaster > 0 && thisNewEntityMaster > 0){
                    if(thisNewEntityMaster < currentLatestMaster){
                        //Already have this update
                        //skip
                        break;
                    }else if(thisNewEntityMaster == currentLatestMaster){
                        //TODO: Change this 0 to the the existing entity's stored Date
                        //You may need to have a method in manager to return PK object and
                        //user thisNewEntity's pk object to search via manager.GetviaPK..
                        // and compare stored date here to resolve
                        if(thisNewEntity.getStoredDate() < 0){
                            //Have a newer update. Lets keep what we have
                            break;
                        }
                    }
                }
            }else{
                //Its a new entry, let it create it, there should be no conflicts
            }

            thisManager.persist(dbContext, thisNewEntity, false);
        }
        JSONObject responseJSON;
        resultStatus = 200; //regardless of conflicts, its gonna be 200
        Map<String, String> responseHeaders = null;
        String resultForClient = null;
        InputStream responseData = null;
        long responseLength = 0;
        if(conflictEntries != null && conflictEntries.size() > 0){
            /*
            Add conflictEntries to response below
             */
            responseJSON = new JSONObject();
            JSONArray conflictEntitiesJSON = new JSONArray();
            for(NanoLrsModel thisConflictEntry:conflictEntries){
                JSONObject thisConflictEntryJSON =
                        ProxyJsonSerializer.toJson(thisConflictEntry, thisConflictEntry.getClass());
                conflictEntitiesJSON.put(thisConflictEntryJSON);
            }
            responseJSON.put("conflict", conflictEntitiesJSON);

            resultForClient = responseJSON.toString();
            responseData =
                    new ByteArrayInputStream(resultForClient.getBytes("UTF-8"));
            responseLength = resultForClient.length();
        }else{
            responseData = null;
            responseLength = 0;
        }

        /* Create a request result of whats stored, and give back what need to be given..
            The request response should contain :
            a. Any conflict
            b. ?? (anything you can think of ?)
         */
        resultResponse = new UMSyncResult(resultStatus,responseHeaders,
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
     * Handles sync process : gets all entites to be synced from syncstatus seqnum and
     * builds entities list to convert to json array to send in a request to host's
     * syncURL endpoint
     * @param node : The server, client, proxy, etc
     * @return
     */
    public static UMSyncResult startSync(User thisUser, Node node, Object dbContext) throws SQLException{
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

        //SyncStatus manager
        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);

        //Map of Entity and latestSeq got so we can update sync status upon sync success
        Map<Class, Long> entityToLatestLocalSeqNum = new HashMap<>();
        Map<Class, Long> entityToLatestMasterSeqNum = new HashMap<>();

        //testing:
        long getUserSentSeqForThisHost =
                syncStatusManager.getSentStatus(node.getHost(), User.class, dbContext);

        JSONArray pendingJSONEntites = new JSONArray(); //entities
        JSONArray pendingJSONInfo = new JSONArray(); //entities info
        JSONObject pendingEntitiesWithInfo = new JSONObject(); //entities with entities info
        //Scan through every Syncable entity..
        for(Class syncableEntity : SYNCABLE_ENTITIES) {
            //Get its manager
            Class managerClass = proxyClassToManagerMap.get(syncableEntity);
            NanoLrsManagerSyncable syncableEntityManager = (NanoLrsManagerSyncable)
                    PersistenceManager.getInstance().getManager(managerClass);
            
            //Get the primary key
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

            //Get table name
            String tableName = convertCamelCaseNameToUnderscored(
                    Character.toLowerCase(syncableEntity.getSimpleName().charAt(0)) +
                            syncableEntity.getSimpleName().substring(1));

            //Create JSON of this Entity's info used for syncing
            JSONObject thisEntityInfo = new JSONObject();
            thisEntityInfo.put("pCls", syncableEntity.getName());
            thisEntityInfo.put("tableName", tableName);
            thisEntityInfo.put("count", 0);
            thisEntityInfo.put("pk", pkField);

            //Add this entity to an array list of entity info for this sync
            pendingJSONInfo.put(thisEntityInfo);

            //Get the last sync status for this host
            long getSyncableEntitySeqForThisHost =
                    syncStatusManager.getSentStatus(node.getHost(), syncableEntity, dbContext);
            
            //Get pendingEntities since the last sync status for this host
            List<NanoLrsModel> pendingEntitesToBeSynced =
                    syncableEntityManager.getAllSinceSequenceNumber(
                    thisUser, dbContext, node.getHost(), getSyncableEntitySeqForThisHost);

            long latestSeqNumToUpdateSyncStatus = -1;
            long latestMasterSeqNumToUpdateSyncStatus = -1;

            //Populate Entities and Info JSONArrays
            if(!pendingEntitesToBeSynced.isEmpty()){
                Iterator<NanoLrsModel> pendingEntitesIterator = pendingEntitesToBeSynced.iterator();
                while(pendingEntitesIterator.hasNext()){
                    NanoLrsModelSyncable thisEntity =
                            (NanoLrsModelSyncable)pendingEntitesIterator.next();

                    //Update latestSeqNum given back:
                    if(latestSeqNumToUpdateSyncStatus == -1){
                        latestSeqNumToUpdateSyncStatus = thisEntity.getLocalSequence();
                    }else{
                        if(latestSeqNumToUpdateSyncStatus < thisEntity.getLocalSequence()){
                            latestSeqNumToUpdateSyncStatus = thisEntity.getLocalSequence();
                        }
                    }
                    JSONObject thisEntityInJSON =
                            ProxyJsonSerializer.toJson(thisEntity, syncableEntity);
                    //Increment count for every entity's type in info
                    for(int i=0;i<pendingJSONInfo.length();i++){
                        if (pendingJSONInfo.getJSONObject(i).getString("pCls").equals(syncableEntity.getName())) {
                            int currentCount = pendingJSONInfo.getJSONObject(i).getInt("count") + 1;
                            pendingJSONInfo.getJSONObject(i).put("count", currentCount);
                        }
                    }
                    if(thisEntityInJSON != null){
                        pendingJSONEntites.put(thisEntityInJSON);
                    }
                }
            }

            //Add the latestSeqNum to this class in a map so upon sync success
            // we can update SyncStatus
            if(latestSeqNumToUpdateSyncStatus > 0) {
                entityToLatestLocalSeqNum.put(syncableEntity, latestSeqNumToUpdateSyncStatus);
            }

            //TODO: fix this, remove this, but take into account MasterSeqNum
            if(latestMasterSeqNumToUpdateSyncStatus > 0) {
                //entityToLatestMasterSeqNum.put(syncableEntity, latestMasterSeqNumToUpdateSyncStatus);
            }
        }

        //Headers if any..
        Map <String, String> headers = new HashMap<String, String>();
        headers.put("someheader", "somevalue");

        //Parameters if any..
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("someparameter", "somevalue");

        //Create a JSONObject with entities JSONArray and info JSONArray
        //to be sent in request body
        pendingEntitiesWithInfo.put("data", pendingJSONEntites);
        pendingEntitiesWithInfo.put("info", pendingJSONInfo);


        //Make a request with the JOSN in POST body and return the
        //UMSyncResult
        UMSyncResult syncResult = makeSyncRequest(node.getUrl(), "POST", headers, parameters,
                pendingEntitiesWithInfo, "application/json", null );

        //Update the SyncStatus with latest value of seq num for
        // this host and every entity
        if(syncResult.getStatus() == 200){
            Iterator<Map.Entry<Class, Long>> entityToLatestLocalSeqNumIterator =
                    entityToLatestLocalSeqNum.entrySet().iterator();
            while(entityToLatestLocalSeqNumIterator.hasNext()){
                Map.Entry<Class, Long> thisEntityToLatestLocalSeqNumEntry = entityToLatestLocalSeqNumIterator.next();
                syncStatusManager.updateSyncStatusSeqNum(node.getHost(),
                        thisEntityToLatestLocalSeqNumEntry.getKey(),
                        thisEntityToLatestLocalSeqNumEntry.getValue(),
                        -1,
                        dbContext);
            }

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

        //Check if response has conflicts
        InputStream syncResultResponseStream = syncResult.getResponseData();
        String syncResultResponse = convertStreamToString(syncResultResponseStream, "UTF-8");
        if(!syncResultResponse.isEmpty()){
            JSONObject syncResultAllResponseJSON = new JSONObject(syncResultResponse);
            JSONObject syncResultConflictJSON = new JSONObject();
            syncResultConflictJSON = null;
            if(syncResultAllResponseJSON.optJSONObject("data") != null){
                if(syncResultAllResponseJSON.optJSONObject("data").optJSONObject("data") != null){
                    if(syncResultAllResponseJSON.getJSONObject("data").getJSONObject("data").has("conflict")){
                        syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("data").getJSONObject("data").getJSONObject("conflict");
                    }
                }else{
                    if(syncResultAllResponseJSON.optJSONObject("data").has("conflict")){
                        syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("data").getJSONObject("conflict");
                    }
                }
            }
            if(syncResultAllResponseJSON.optJSONObject("json")!=null){
                if(syncResultAllResponseJSON.getJSONObject("json").optJSONObject("data") != null){
                    if(syncResultAllResponseJSON.getJSONObject("json").getJSONObject("data").has("conflict")){
                        syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("json").getJSONObject("data").getJSONObject("conflict");
                    }
                }
                if(syncResultAllResponseJSON.getJSONObject("json").has("conflict")){
                    syncResultConflictJSON = syncResultAllResponseJSON.getJSONObject("json").getJSONObject("conflict");
                }
            }

            if(syncResultConflictJSON != null){
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
                //TODO: Handle Conflict entities as they come back.
            }
        }

        return syncResult;
    }

    /**
     * Store syncable entities.
     * TODO: Put this in one common place , OR
     * TODO: Find a way to get all from NanoLrsModelSyncable extentsion.
     */
    public static Class[] SYNCABLE_ENTITIES = new Class[]{
            User.class,
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
            setHeaders(con, headers);
            if(contentType != null) {
                //For JSON it is: application/json
                con.setRequestProperty("Content-Type", contentType);
                con.setRequestProperty("Accept", contentType);

            }
            //if(!dataJSONArray.isNull(0) && content == null){
            if(!dataJSON.equals(null) && dataJSON.length()>0 && content == null){
                con.setFixedLengthStreamingMode(dataJSON.toString().length());

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
                byte[] postData = paramString.getBytes("UTF-8");
                int postDataLength = postData.length;
                con.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
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
                con.disconnect();
            }
        }

        return response;
    }
}
