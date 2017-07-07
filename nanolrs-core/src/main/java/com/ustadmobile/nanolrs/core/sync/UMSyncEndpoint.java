package com.ustadmobile.nanolrs.core.sync;

import com.ustadmobile.nanolrs.core.ProxyJsonSerializer;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

        /*
        proxyNameToClassMap.put(Person.class.getName(), Person.class);
        proxyClassToManagerMap.put(Person.class, PersonManager.class);
        proxyNameToClassMap.put(Clazz.class.getName(), Clazz.class);
        proxyClassToManagerMap.put(Clazz.class, ClazzManager.class);
        proxyNameToClassMap.put(School.class.getName(), School.class);
        proxyClassToManagerMap.put(School.class, SchoolManager.class);
        */
    }

    public static String convertStreamToString(InputStream is, String encoding) {
        java.util.Scanner s = new java.util.Scanner(is, encoding).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Handles incoming sync requests. Essentially an endpoint to process request and
     * update database and handle it
     * @param inputStream
     * @param headers
     * @return
     */
    public static UMSyncResult handleIncomingSync(InputStream inputStream, Map headers, Object dbContext)
            throws SQLException{
        UMSyncResult result = null;

        /*
        Steps:
        1. Validate headers and param and input stream
        2. Get the json array from input stream
        3. convert to entityies
        4. get number
        5. add to db (persist)
        6. Send repsonse
        */

        String streamString = convertStreamToString(inputStream, "UTF-8");
        Map<NanoLrsModelSyncable, String> allNewEntitiesMap =
                new HashMap<NanoLrsModelSyncable, String>();
        JSONArray entitiesJSON = new JSONArray(streamString);
        for(int i=0; i < entitiesJSON.length(); i++){
            JSONObject entityJSON = entitiesJSON.getJSONObject(i);
        //}
        //Iterator entitiesJSONIterator = entitiesJSON.iterator();
        //while(entitiesJSONIterator.hasNext()){
            //JSONObject entityJSON = (JSONObject)entitiesJSONIterator.next();
            NanoLrsModel thisEntity = ProxyJsonSerializer.toEntity(entityJSON, dbContext);
            String thisProxyClass =
                    entityJSON.getString(ProxyJsonSerializer.PROXY_CLASS_JSON_FIELD);

            allNewEntitiesMap.put((NanoLrsModelSyncable)thisEntity, thisProxyClass);
        }

        Iterator<Map.Entry<NanoLrsModelSyncable, String>> allNewEntitiesMapIterator = 
                allNewEntitiesMap.entrySet().iterator();
        while(allNewEntitiesMapIterator.hasNext()){
            Map.Entry<NanoLrsModelSyncable, String> thisNewEntityMap = (Map.Entry)
                    allNewEntitiesMapIterator.next();
            NanoLrsModelSyncable thisNewEntity = thisNewEntityMap.getKey();
            String thisProxyClassName = thisNewEntityMap.getValue();

            Class thisProxyClass = proxyNameToClassMap.get(thisProxyClassName);
            Class thisManagerClass = proxyClassToManagerMap.get(thisProxyClass);
            NanoLrsManagerSyncable manager = (NanoLrsManagerSyncable)
                    PersistenceManager.getInstance().getManager(thisManagerClass);
            manager.persist(dbContext, thisNewEntity);
        }

        // Create a request of whats stored, and give back what need to be given..

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

        //Make a request with the JOSN in POST body
        Map <String, String> headers = new HashMap<String, String>();
        headers.put("someheader", "somevalue");

        return makeSyncRequest(syncURL, "POST", headers, null, pendingJSONEntitesToBeSynced,
                "application/json", null );

    }

    public static Class[] SYNCABLE_ENTITIES = new Class[]{
            User.class,
    };

    private static void setHeaders(HttpURLConnection connection, Map headers) throws IOException {
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            connection.setRequestProperty(pair.getKey().toString(), pair.getValue().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public static UMSyncResult makeSyncRequest(String destURL, String method, Map headers,
               Map parameters, JSONArray dataJSONArray, String contentType, byte[] content) {
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
            if(!dataJSONArray.isNull(0) && content == null){
                con.setFixedLengthStreamingMode(dataJSONArray.toString().length());

                outw = new OutputStreamWriter(con.getOutputStream());
                outw.write(dataJSONArray.toString());
                outw.flush();
                //outw.close();
                //outw = null;
            }else {
                con.setFixedLengthStreamingMode(content.length);

                out = con.getOutputStream();
                out.write(content);
                out.flush();
                out.close();
                out = null;
            }




            int statusCode = con.getResponseCode();
            response.setStatus(statusCode);
            response.setResponse(con.getResponseMessage());

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
