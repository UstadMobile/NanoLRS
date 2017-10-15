package com.ustadmobile.nanolrs.android.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.generated.EntitiesToTable;
import com.ustadmobile.nanolrs.ormlite.generated.model.UserEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiDocumentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStateEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;


import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by mike on 9/6/16.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {


    public static final String LOGTAG = "NanoLRS/DatabaseHelper";

    /**
     * Database Name to be used: nanolrs.db was used by previous versons.  From DATABASE_VERSION 11
     */
    private static final String DATABASE_NAME="nanolrs21.db";

    private static final int DATABASE_VERSION = 21;

    private Context context;

    private Dao<XapiStatementEntity, Integer> mXapiStatementDao;

    //TODO: Get these from build properties
    public static String DEFAULT_MAIN_SERVER_HOST_NAME = "umcloud1svlt";
    public static String DEFAULT_MAIN_SERVER_NAME = "umcloud1 servlet";
    public static String DEFAULT_MAIN_SERVER_ROLE = "main";
    //public static String DEFAULT_MAIN_SERVER_URL = "https://umcloud1.ustadmobile.com:8545/syncendpoint/";
    public static String DEFAULT_MAIN_SERVER_URL =
            "https://umcloud1.ustadmobile.com:8086/syncendpoint/";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
        this.context = context;
    }

    /*
    public static Class[] TABLE_CLASSES = new Class[]{ XapiActivityEntity.class, XapiAgentEntity.class,
            XapiStatementEntity.class, XapiVerbEntity.class, XapiForwardingStatementEntity.class,
            UserEntity.class, XapiDocumentEntity.class, XapiStateEntity.class, ChangeSeqEntity.class,
            SyncStatusEntity.class, NodeEntity.class, UserCustomFieldsEntity.class
    };
    */

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");

            for(Class clazz : EntitiesToTable.TABLE_CLASSES) {
                TableUtils.createTableIfNotExists(connectionSource, clazz);
                //TableUtils.createTable(connectionSource, clazz);
            }

            try {
                checkAndCreateMainNode();
                checkAndCreateThisNode();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }catch(SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "can't create database", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {

            if(oldVersion < 9) {
                TableUtils.createTable(connectionSource, UserEntity.class);
            }

            if(oldVersion < 10) {
                TableUtils.createTable(connectionSource, XapiDocumentEntity.class);
                TableUtils.createTable(connectionSource, XapiStateEntity.class);
            }

            if(oldVersion < 12) {
                //Execute raw SQL to put an index on the timestamp property of statements
                getDao(XapiStatementEntity.class).executeRaw(
                        "CREATE INDEX IF NOT EXISTS xapi_statements_timestamp_idx on xapi_statements ( timestamp )");
            }
        }catch(SQLException e) {
            Log.e(LOGTAG, "Exception onUpgrade", e);
            throw new RuntimeException(e);
        }
    }

    private void checkAndCreateMainNode() throws SQLException {
        String mainNodeName = DEFAULT_MAIN_SERVER_NAME;
        String mainNodeHostName = DEFAULT_MAIN_SERVER_HOST_NAME;
        String mainNodeEndpointUrl = DEFAULT_MAIN_SERVER_URL;
        String mainNodeRole = DEFAULT_MAIN_SERVER_ROLE;
        String mainNodeUUID = UUID.randomUUID().toString();

        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);

        if(!nodeManager.doesThisMainNodeExist(mainNodeHostName, context)){
            Node mainNode = (Node)nodeManager.makeNew();
            mainNode.setUUID(mainNodeUUID);
            mainNode.setMaster(true);
            mainNode.setRole(mainNodeRole);
            mainNode.setUrl(mainNodeEndpointUrl);
            mainNode.setName(mainNodeName);
            mainNode.setHost(mainNodeHostName);
            mainNode.setStoredDate(System.currentTimeMillis());
            nodeManager.persist(context,mainNode);
        }else{
            //Check if URL is up to date (eg: http -> https)
            Node mainNode = nodeManager.getMainNode(mainNodeHostName, context);
            if(!mainNode.getUrl().equals(mainNodeEndpointUrl)){
                //update url
                mainNode.setUUID(mainNodeEndpointUrl);
                nodeManager.persist(context,mainNode);
            }
        }
    }

    private void checkAndCreateThisNode() throws SQLException {
        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);
        String thisNodeUUID = UUID.randomUUID().toString();
        String thisNodeUrl = "set-my-url";
        //TODODone: Check if we want to get device's name & info
        //Update: Sticking with UUID for now
        String thisNodeName = "node:" + thisNodeUUID;
        String thisNodeHostName = "host:" + thisNodeUUID;

        try{
            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
            thisNodeHostName = "device:" + reqString.replaceAll(" ", "_");

        }catch (Exception e){
            System.out.println("Cannot get device info exception : " + e);
        }

        nodeManager.createThisDeviceNode(thisNodeUUID, thisNodeName, thisNodeHostName,
                thisNodeUrl, false, false, context);
    }

}
