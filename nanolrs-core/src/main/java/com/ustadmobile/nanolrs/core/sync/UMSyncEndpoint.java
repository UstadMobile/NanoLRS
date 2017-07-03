package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.sql.SQLException;
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

        /*
        proxyNameToClassMap.put(Person.class.getName(), Person.class);
        proxyClassToManagerMap.put(Person.class, PersonManager.class);
        proxyNameToClassMap.put(Clazz.class.getName(), Clazz.class);
        proxyClassToManagerMap.put(Clazz.class, ClazzManager.class);
        proxyNameToClassMap.put(School.class.getName(), School.class);
        proxyClassToManagerMap.put(School.class, SchoolManager.class);
        */
    }


    /**
     * Handles incoming sync requests. Essentially an endpoint to process request and
     * update database and handle it
     * @param inputStream
     * @param headers
     * @return
     */
    public UMSyncResult handleIncomingSync(InputStream inputStream, Map headers, Object dbContext){
        UMSyncResult result = null;

        return result;
    }

    /**
     * Handles sync process : gets all entites to be synced from syncstatus seqnum and
     * builds entities list to convert to json array to send in a request to host's
     * syncURL endpoint
     * @param syncURL
     * @param host
     * @return
     */
    public static UMSyncResult startSync(String syncURL, String host, Object dbContext) throws SQLException{
        //TODO: this
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

        UMSyncResult result = null;
        User this_user = null;

        SyncStatusManager syncStatusManager=
                PersistenceManager.getInstance().getManager(SyncStatusManager.class);
        //testing:
        long getUserSentSeqForThisHost =
                syncStatusManager.getSentStatus(host, User.class, dbContext);

        JSONArray pendingJSONEntitesToBeSynced = new JSONArray();
        for(Class syncableEntity : SYNCABLE_ENTITIES) {
            long getSyncableEntitySeqForThisHost =
                    syncStatusManager.getSentStatus(host, syncableEntity, dbContext);


            Class managerClass = proxyClassToManagerMap.get(syncableEntity);
            NanoLrsManagerSyncable syncableEntityManager = (NanoLrsManagerSyncable)
                    PersistenceManager.getInstance().getManager(managerClass);


            List<NanoLrsModel> pendingEntitesToBeSynced =
                    syncableEntityManager.getAllSinceSequenceNumber(
                    this_user, dbContext, host, getSyncableEntitySeqForThisHost);

            if(!pendingEntitesToBeSynced.isEmpty()){

                Iterator<NanoLrsModel> pendingEntitesIterator = pendingEntitesToBeSynced.iterator();
                while(pendingEntitesIterator.hasNext()){
                    JSONObject thisEntityInJSON =
                            ProxyJsonSerializer.toJson(pendingEntitesIterator.next(), syncableEntity);
                    if(thisEntityInJSON != null){
                        pendingJSONEntitesToBeSynced.put(thisEntityInJSON);
                    }
                }
            }

        }

        //Make a request with the JOSN:

        return result;
    }

    public static Class[] SYNCABLE_ENTITIES = new Class[]{
            User.class,
    };
}
