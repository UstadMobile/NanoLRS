package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
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
        return pkField;
    }

    /**
     * Common method to return manager from a class
     * @param syncableEntity eg: User.class
     * @return
     */
    public static NanoLrsManagerSyncable getManagerFromClass(Class syncableEntity){
        Class managerClass = proxyClassToManagerMap.get(syncableEntity);
        NanoLrsManagerSyncable syncableEntityManager = (NanoLrsManagerSyncable)
                PersistenceManager.getInstance().getManager(managerClass);
        return syncableEntityManager;
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
        return convertCamelCaseNameToUnderscored(
                Character.toLowerCase(syncableEntity.getSimpleName().charAt(0)) +
                        syncableEntity.getSimpleName().substring(1));
    }

    public static String convertStreamToString(InputStream is, String encoding) {
        java.util.Scanner s = new java.util.Scanner(is, encoding).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
        7. Resolve conflicts (if any)
        7. Get updates for senderNode
        8. Send back any updates, conflicts, in the response body
        9. Update SyncStatus table
        */

        //The return result and status of the incoming request's sync on this node
        UMSyncResult resultResponse = null;
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
        Map<NanoLrsModelSyncable, String> allReturnEntitiesMap = new HashMap<>();

        //Map of <Entity Proxy Name, Pre-Sync ChangeSeq No.>
        //We store this such that we can get back to it while
        // updating the entity's sync entities and we can then
        //  give it a +1.
        Map<String, Long> preSyncEntitySeqNumMap = new HashMap<>();

        //Pre sync change seq for ALL syncable entities
        Map<String, Long> preSyncAllEntitiesSeqMap = new HashMap<>();

        //Post sync change seq for ALL syncable entities
        Map<String, Long> postSyncAllEntitiesSeqMap = new HashMap<>();

        //List of entities that are conflicts
        // Added to this array so we can return it if cannot be resolved.
        List<NanoLrsModelSyncable> conflictEntries = new ArrayList<NanoLrsModelSyncable>();

        //Get this device/node
        Node thisNode = null;
        thisNode = nodeManager.getThisNode(dbContext);

        //Get this user
        String userUuid = headers.get(HEADER_USER_UUID).toString();
        User thisUser = null;
        thisUser = userManager.findById(dbContext,userUuid);

        //Get all syncable entities pre sync seq and put it in preSyncSeqMap
        for(Class thisEntity:SYNCABLE_ENTITIES){
            //Pre-Sync : Add existing ChangeSeq value to preSyncAllEntitiesSeqMap
            String proxyClassName = thisEntity.getName();
            String tableName = getTableNameFromClass(thisEntity);
            long preSyncEntitySeqNum =
                    changeSeqManager.getNextChangeByTableName(tableName, dbContext);
            preSyncAllEntitiesSeqMap.put(proxyClassName, preSyncEntitySeqNum);
        }

        //Convert Inputstream to JSONObjects
        //Convert inputstream->string->entities json array
        String streamString = null;
        try {
            streamString = convertStreamToString2(inputStream, UTF_ENCODING);
        } catch (IOException e) {
            e.printStackTrace();
            //Cannot proceed. Stream is fauly. Skip?
        }

        //Get data and info separately
        JSONObject entitiesWithInfoJSON = new JSONObject(streamString);
        JSONArray entitiesJSON = entitiesWithInfoJSON.getJSONArray(RESPONSE_ENTITIES_DATA);
        JSONArray entitiesInfoJSON = entitiesWithInfoJSON.getJSONArray(RESPONSE_ENTITIES_INFO);

        //Create Entity Map of <Entity Object, Proxy Class Name>
        for(int i=0; i < entitiesJSON.length(); i++){
            JSONObject entityJSON = entitiesJSON.getJSONObject(i);
            NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
            String thisProxyClass =
                    entityJSON.getString(ProxyJsonSerializer.PROXY_CLASS_JSON_FIELD);
            allNewEntitiesMap.put((NanoLrsModelSyncable)thisEntity, thisProxyClass);
        }

        //Reserve set of ChangeSeq numbers for every entity type
        //Increment every Entity's ChangeSeq by count of new updates
        for(int j=0;j<entitiesInfoJSON.length();j++){
            JSONObject thisEntityInfoJSON = entitiesInfoJSON.getJSONObject(j);
            String proxyClassName = thisEntityInfoJSON.getString(ENTITY_INFO_CLASS_NAME);
            String tableName = thisEntityInfoJSON.getString(ENTITY_INFO_TABLE_NAME);
            int count = thisEntityInfoJSON.getInt(ENTITY_INFO_COUNT);

            //Pre-Sync : Add existing ChangeSeq value to preSyncEntitySeqNumMap
            long preSyncEntitySeqNum =
                    changeSeqManager.getNextChangeByTableName(tableName, dbContext);
            preSyncEntitySeqNumMap.put(proxyClassName, preSyncEntitySeqNum);

            //Increment the ChangeSeq by count of new & update entities
            changeSeqManager.getNextChangeAddSeqByTableName(tableName, count, dbContext);
        }

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
            Class thisManagerClass = proxyClassToManagerMap.get(thisProxyClass);
            NanoLrsManagerSyncable thisManager = (NanoLrsManagerSyncable)
                    PersistenceManager.getInstance().getManager(thisManagerClass);

            //Set entity's change seq from available pool (preSyncEntitySeqNumMap)
            long thisEntityChangeSeq = preSyncEntitySeqNumMap.get(thisProxyClassName);
            long thisNewEntityNewSeq = thisEntityChangeSeq + 1; //this is the new seq num
            preSyncEntitySeqNumMap.put(thisProxyClassName, thisNewEntityNewSeq); //set the next one
            thisNewEntity.setLocalSequence(thisNewEntityNewSeq);

            //If master, update master sequence as well..
            if (thisNode != null) {
                if (thisNode.isMaster()) {
                    thisNewEntity.setMasterSequence(thisNewEntityNewSeq);
                    //TODO: Store this info and send it back somehow
                }
            }

            //Get primary key field of this entity:
            String pkField = null;
            for(int k=0;k<entitiesInfoJSON.length();k++){
                JSONObject eInfoJSON = entitiesInfoJSON.getJSONObject(k);
                if(eInfoJSON.getString(ENTITY_INFO_CLASS_NAME).equals(thisProxyClassName)){
                    pkField = eInfoJSON.getString(ENTITY_INFO_PRIMARY_KEY);
                    if (!pkField.isEmpty()){
                        pkField = "get" + Character.toUpperCase(pkField.charAt(0))
                                + pkField.substring(1);
                    }
                    break;
                }
            }

            //Get local version and check if its new or an update : Using reflection
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

            ////////////////////////////////////
            ///  UPDATE/CONFLICT RESOLUTION  ///
            ////////////////////////////////////
            /* Accept if:
                a. Has a new Master Seq >  Current Master Seq
                b. Is a new entry
                c. Has the same master seq, but modified later
             If conflict: array them which will be sent back in response
             */

            //If it is an update, check for conflicts
            boolean doIPersist = true;
            int wePutConflictsHere = conflictEntries.size();

            //Get sync status for this host and entity and its latest sent sync seq
            SyncStatus ss = (SyncStatus)
                    syncStatusManager.getSyncStatus(node.getHost(),thisProxyClass, dbContext);
            Long lastSyncSeq = syncStatusManager.getSentStatus(node.getHost(),
                    thisProxyClass, dbContext);

            boolean newEntry = false;
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
                        break;
                    }else if(thisNewEntityMaster == currentLatestMaster){
                        //We have an update that is from master and so is ours
                        //We also have both out masters the same. Which cannot be possible
                        //TODO: Change this 0 to the the existing entity's stored Date
                        System.out.println("Sync Conflict. Both entities have the same master.\n" +
                                "We cannot keep the update. We ignore this.\n");
                        break;
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
                            break;
                        }else{
                            System.out.println(" .. but incoming is newer. Allowing..");
                        }

                    }else{
                        //Accept the new entity because it has a higher master..
                    }
                }
                else{
                    //neither of them have been synced with master.
                    //Check if thisNewEntity's node is a proxy

                    if(node.isProxy()){
                        //The sender is a proxy and its got an update for an entry
                        //that has never been synced with master
                        //We gotta check if proxy's update is more recent than ours

                        if(thisNewEntity.getLocalSequence() > lastSyncSeq){
                            //WAIT the sync wont come unless its greater than.
                            //Maybe we don't really even need this check..
                            //TODO: check
                            System.out.println("\nIncoming Sync Resolution: From proxy\n" +
                                    "Request from proxy: is higher. Accepting..\n");
                        }else{
                            System.out.println("\nIncoming Sync resolution:" +
                                    "Sender is proxy.\n" +
                                    "Sender's are already in the system. They shouldn't have been sent.\n" +
                                    "Not updating this entry.\n");
                            break;
                        }
                    }else{
                        //The node sending this is not master or a proxy.
                        // We ideally don't take in entities from non master/proxy
                        // Suppose we do, if can accept the ones that are newer
                        if(newStoredDate > currentStoredDate){
                            System.out.println("\nIncoming Sync conflict:" +
                                    "Sender is not master or proxy.\n" +
                                    "Sender's entries are more recent than what I have.\n" +
                                    "Not updating it since not a proxy or master..\n");
                            break;
                        }else{
                            System.out.println("\nIncoming Sync resultion:" +
                            "Sender is not master or proxy.\n" +
                                    "Senders entries are not more recent. " +
                                            "\nNot updating it since Not a proxy or master.\n"
                            );
                            break;
                        }
                    }
                }
            }else{
                //Its a new entry, let it create it, there should be no conflicts
                //since this primary key does not exist on this node.
                newEntry = true;
            }

            //persist (update/create the entity without doing a +1 to its change seq)
            //because we have already set it above (from pool)
            if(doIPersist) {
                thisManager.persist(dbContext, thisNewEntity, false);
            }

            ////////////////////////////////////
            ///     UPDATE SYNC STATUS       ///
            ////////////////////////////////////
            long currentSent = ss.getSentSeq();
            if(thisNewEntityNewSeq > currentSent){
                ss.setSentSeq(thisNewEntityNewSeq);
                syncStatusManager.persist(dbContext, ss);
            }

            //TODO: DO we need to update received? Or is that only for proxy?
            /*
            long currentRec = ss.getReceivedSeq();
            if(thisNewEntityNewSeq > currentRec) {
                ss.setReceivedSeq(thisNewEntityNewSeq);
                syncStatusManager.persist(dbContext, ss);
            }
            */


        }

        //Get all syncable entities post sync seq and put it in postSyncSeqMap
        for(Class thisEntity:SYNCABLE_ENTITIES){
            //Pre-Sync : Add existing ChangeSeq value to preSyncEntitySeqNumMap
            String proxyClassName = thisEntity.getName();
            String tableName = getTableNameFromClass(thisEntity);
            long preSyncEntitySeqNum =
                    changeSeqManager.getNextChangeByTableName(tableName, dbContext);
            postSyncAllEntitiesSeqMap.put(proxyClassName, preSyncEntitySeqNum);
        }


        ////////////////////////////////
        // CONSTRUCT RETURN ENTITIES  //
        ////////////////////////////////

        /* Create a request result of whats stored, and give back what need to be given..
            The request response should contain :
            a. Any conflict
            b. Any data meant for the user to be sent back
            c. Any more ??
         */

        ///UNDER CONSTRUCTION

        //Map of Entity and latestSeq got so we can update sync status upon sync success
        Map<Class, Long> entityToLatestSeqReturn = new HashMap<>();

        for(Class syncableEntity : SYNCABLE_ENTITIES) {
            //Get its manager
            NanoLrsManagerSyncable syncableEntityManager = getManagerFromClass(syncableEntity);

            //Get the primary key
            String pkField = getPrimaryKeyFromClass(syncableEntity);

            //Get table name
            String tableName = getTableNameFromClass(syncableEntity);

            //Create JSON of this Entity's info used for syncing
            JSONObject jsonInfo = createJSONFromClass(syncableEntity);

            //Add this entity to an array list of entity info for this sync
            sendTheseInfoBack.put(jsonInfo);

            //Get sync status for this entity and host
            SyncStatus ss = (SyncStatus) syncStatusManager.getSyncStatus(
                    node.getHost(), syncableEntity, dbContext);

            //Get the last sync status for this host
            long preSyncChangeSeq = preSyncAllEntitiesSeqMap.get(syncableEntity.getName());

            long lastSyncStatusSent = ss.getSentSeq();

            //We need to get entities between lastSyncStatusSent <---> preSyncChangeSeq
            //Get pendingEntities between those two for this entity and this host:
            List<NanoLrsModel> pendingEntitiesToReturn =
                    syncableEntityManager.getAllSinceTwoSequenceNumber(thisUser, node.getHost(),
                            lastSyncStatusSent, preSyncChangeSeq, dbContext);

            //Get pendingEntities since the last sync status for this host
            //List<NanoLrsModel> pendingEntitiesToReturn =
            //        syncableEntityManager.getAllSinceSequenceNumber(
            //                thisUser, dbContext, node.getHost(), lastSyncStatusSent);

            long latestSeqNumReturned = -1;
            long latestMSeqNumReturned = -1;

            //Populate Entities and Info JSONArrays
            if(pendingEntitiesToReturn != null && !pendingEntitiesToReturn.isEmpty()){
                Iterator<NanoLrsModel> pendingEntitesIterator = pendingEntitiesToReturn.iterator();
                while(pendingEntitesIterator.hasNext()){
                    NanoLrsModelSyncable thisEntity =
                            (NanoLrsModelSyncable)pendingEntitesIterator.next();

                    //Update latestSeqNum given back:
                    if(latestSeqNumReturned == -1){
                        latestSeqNumReturned = thisEntity.getLocalSequence();
                    }else{
                        if(latestSeqNumReturned < thisEntity.getLocalSequence()){
                            latestSeqNumReturned = thisEntity.getLocalSequence();
                        }
                    }
                    JSONObject thisEntityInJSON =
                            ProxyJsonSerializer.toJson(thisEntity, syncableEntity);

                    //TODO: Probably better than digging count, use a variable: Mike
                    //Increment count for every entity's type in info
                    for(int i=0;i<sendTheseInfoBack.length();i++){
                        if (sendTheseInfoBack.getJSONObject(i).getString(ENTITY_INFO_CLASS_NAME).equals(syncableEntity.getName())) {
                            int currentCount = sendTheseInfoBack.getJSONObject(i).getInt(ENTITY_INFO_COUNT) + 1;
                            sendTheseInfoBack.getJSONObject(i).put(ENTITY_INFO_COUNT, currentCount);
                        }
                    }
                    if(thisEntityInJSON != null){
                        sendTheseEntitiesBack.put(thisEntityInJSON);
                    }

                    ////////////////////////////////////
                    ///     UPDATE SYNC STATUS       ///
                    ////////////////////////////////////
                    long currentSentSeq = ss.getSentSeq();
                    //update only the latest
                    if(latestSeqNumReturned > currentSentSeq){
                        ss.setSentSeq(latestSeqNumReturned);
                        syncStatusManager.persist(dbContext, ss);
                    }
                }
            }

            //Add the latestSeqNum to this class in a map so upon sync success
            // we can update SyncStatus
            if(latestSeqNumReturned > 0) {
                entityToLatestSeqReturn.put(syncableEntity, latestSeqNumReturned);
                //TODO: Can we get rid of this ?
            }

            //TODO: fix this, remove this, but take into account MasterSeqNum
            //TODO: Can we get rid of this?
            // Update: Might not need this after all since Master is only on master
            // and any update locally nulls/0s master seq
            if(latestMSeqNumReturned > 0) {
                //entityToLatestMasterSeqNum.put(syncableEntity, latestMasterSeqNumToUpdateSyncStatus);
            }
        }
        //////END OF CONSTRUCTION

        ////////////////////////////////
        //   CONSTRUCT THE RESPONSE   //
        ////////////////////////////////
        resultStatus = 200; //regardless of conflicts, its gonna be 200
        Map<String, String> responseHeaders = new HashMap<>();

        String isNodeMaster, isNodeProxy;
        if(node.isMaster()){
            isNodeMaster = "true";
        }else{
            isNodeMaster = "false";
        }
        if(node.isProxy()){
            isNodeProxy = "true";
        }else{
            isNodeProxy = "false;";
        }
        responseHeaders.put(HEADER_USER_USERNAME, thisUser.getUsername());
        responseHeaders.put(HEADER_USER_PASSWORD, thisUser.getPassword());
        responseHeaders.put(HEADER_USER_IS_NEW, "false");
        responseHeaders.put(HEADER_USER_UUID, thisUser.getUuid());

        responseHeaders.put(HEADER_NODE_ROLE, node.getRole());
        responseHeaders.put(HEADER_NODE_HOST, node.getHost());
        responseHeaders.put(HEADER_NODE_URL, node.getUrl());
        responseHeaders.put(HEADER_NODE_NAME, node.getName());
        responseHeaders.put(HEADER_NODE_UUID, node.getUUID());
        responseHeaders.put(HEADER_NODE_ISMASTER, isNodeMaster);
        responseHeaders.put(HEADER_NODE_ISPROXY, isNodeProxy);

        String resultForClient = null;
        InputStream responseData = null;
        long responseLength = 0;

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

        //SyncStatus manager
        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
        //NodeManager
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);
        XapiStateManager stateManager=
                PersistenceManager.getInstance().getManager(XapiStateManager.class);
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);

        //TODO: Remove this. For debugging purposes only.
        List allStates = stateManager.getAllEntities(dbContext);
        if(allStates != null && !allStates.isEmpty()){
            System.out.println("State is not empty..");
        }

        //Get this device/node
        //Needs to get created if not set by the device itself. Name can be a combination
        // of device name, location, random uuid.toString(), etc.
        //We use that to check if this device is the master server or not..
        //Also needs some form of authentication, else - anyone can  be master
        Node thisNode = null;
        thisNode = nodeManager.getThisNode(dbContext);


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
            thisEntityInfo.put(ENTITY_INFO_CLASS_NAME, syncableEntity.getName());
            thisEntityInfo.put(ENTITY_INFO_TABLE_NAME, tableName);
            thisEntityInfo.put(ENTITY_INFO_COUNT, 0);
            thisEntityInfo.put(ENTITY_INFO_PRIMARY_KEY, pkField);

            //Add this entity to an array list of entity info for this sync
            pendingJSONInfo.put(thisEntityInfo);

            //Get the last sync status for this host
            long lastSyncSeq =
                    syncStatusManager.getSentStatus(node.getHost(), syncableEntity, dbContext);
            
            //Get pendingEntities since the last sync status for this host
            List<NanoLrsModel> pendingEntitesToBeSynced =
                    syncableEntityManager.getAllSinceSequenceNumber(
                    thisUser, dbContext, node.getHost(), lastSyncSeq);

            long latestSeqNumToUpdateSyncStatus = -1;
            long latestMasterSeqNumToUpdateSyncStatus = -1;

            //Populate Entities and Info JSONArrays
            if(pendingEntitesToBeSynced != null && !pendingEntitesToBeSynced.isEmpty()){
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
                    //TODO: Probably better than digging count, use a variable: Mike
                    //Increment count for every entity's type in info
                    for(int i=0;i<pendingJSONInfo.length();i++){
                        if (pendingJSONInfo.getJSONObject(i).getString(ENTITY_INFO_CLASS_NAME).equals(syncableEntity.getName())) {
                            int currentCount = pendingJSONInfo.getJSONObject(i).getInt(ENTITY_INFO_COUNT) + 1;
                            pendingJSONInfo.getJSONObject(i).put(ENTITY_INFO_COUNT, currentCount);
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

        String username = thisUser.getUsername();
        String password = thisUser.getPassword();
        String thisNodeUuid = thisNode.getUUID();
        String userUuid = thisUser.getUuid();
        String thisNodeHost = thisNode.getHost();
        String thisNodeURL = thisNode.getUrl();
        String thisNodeRole = thisNode.getRole();
        String isNewUser = "false";
        if(thisUser.getMasterSequence() <1 ) {
            isNewUser = "true";
        }

        //Headers if any..
        Map <String, String> headers = new HashMap<String, String>();
        headers.put(HEADER_USER_USERNAME, username);
        headers.put(HEADER_USER_PASSWORD, password);
        headers.put(HEADER_USER_UUID, userUuid);
        headers.put(HEADER_USER_IS_NEW, isNewUser);

        headers.put(HEADER_NODE_UUID, thisNodeUuid);
        headers.put(HEADER_NODE_HOST, thisNodeHost);
        headers.put(HEADER_NODE_URL, thisNodeURL);
        //mostly its "client" as they are the ones that start sync.
        //However that could change, so sending role.
        //TODO: we need to validate these roles somehow
        //mayb: tokens that get authorised like certificates.
        headers.put(HEADER_NODE_ROLE, thisNodeRole);

        //Parameters if any..
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("someparameter", "somevalue");

        //Create a JSONObject with entities JSONArray and info JSONArray
        //to be sent in request body
        pendingEntitiesWithInfo.put(RESPONSE_ENTITIES_DATA, pendingJSONEntites);
        pendingEntitiesWithInfo.put(RESPONSE_ENTITIES_INFO, pendingJSONInfo);


        //Make a request with the JOSN in POST body and return the
        //UMSyncResult
        UMSyncResult syncResult = makeSyncRequest(node.getUrl(), "POST", headers, parameters,
                pendingEntitiesWithInfo, JSON_MIMETYPE, null );

        //Update the SyncStatus with latest value of seq num for
        // this host and every entity
        if(syncResult.getStatus() == 200){
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
        String syncResultResponse = convertStreamToString2(syncResultResponseStream, UTF_ENCODING);
        if(!syncResultResponse.isEmpty()){

            JSONObject syncResultAllResponseJSON = new JSONObject(syncResultResponse);
            JSONObject syncResultConflictJSON = new JSONObject();
            syncResultConflictJSON = null;
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
                //TODO: Handle Conflict and/or entities as they come back.

            }
            JSONArray responseData = new JSONArray();
            JSONArray responseInfo = new JSONArray();
            Map<NanoLrsModelSyncable, String> allNewEntitiesMap = new HashMap<>();

            if(syncResultAllResponseJSON != null){
                if(syncResultAllResponseJSON.optJSONArray(RESPONSE_ENTITIES_DATA) != null){
                    responseData = syncResultAllResponseJSON.getJSONArray(RESPONSE_ENTITIES_DATA);
                }
                if(syncResultAllResponseJSON.optJSONArray(RESPONSE_ENTITIES_INFO) != null){
                    responseInfo = syncResultAllResponseJSON.getJSONArray(RESPONSE_ENTITIES_INFO);
                }
            }

            Map<String, Long> preSyncEntitySeqNumMap = new HashMap<>();
            //Reserve set of ChangeSeq numbers for every entity type
            //Increment every Entity's ChangeSeq by count of new updates
            for(int j=0;j<responseInfo.length();j++){
                JSONObject thisEntityInfoJSON = responseInfo.getJSONObject(j);
                String proxyClassName = thisEntityInfoJSON.getString(ENTITY_INFO_CLASS_NAME);
                String tableName = thisEntityInfoJSON.getString(ENTITY_INFO_TABLE_NAME);
                int count = thisEntityInfoJSON.getInt(ENTITY_INFO_COUNT);

                //Pre-Sync : Add existing ChangeSeq value to preSyncEntitySeqNumMap
                long preSyncEntitySeqNum =
                        changeSeqManager.getNextChangeByTableName(tableName, dbContext);
                preSyncEntitySeqNumMap.put(proxyClassName, preSyncEntitySeqNum);

                //Increment the ChangeSeq by count of new & update entities
                changeSeqManager.getNextChangeAddSeqByTableName(tableName, count, dbContext);
            }

            if(!responseData.isNull(0)){
                for(int i=0;i<responseData.length();i++){
                    JSONObject entityJSON = responseData.getJSONObject(i);
                    NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
                    String thisProxyClassName =
                            entityJSON.getString(ProxyJsonSerializer.PROXY_CLASS_JSON_FIELD);
                    allNewEntitiesMap.put((NanoLrsModelSyncable)thisEntity, thisProxyClassName);

                    Class thisProxyClass = proxyNameToClassMap.get(thisProxyClassName);
                    Class thisManagerClass = proxyClassToManagerMap.get(thisProxyClass);
                    NanoLrsManagerSyncable thisManager = (NanoLrsManagerSyncable)
                            PersistenceManager.getInstance().getManager(thisManagerClass);

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
        }

        return syncResult;
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
}
