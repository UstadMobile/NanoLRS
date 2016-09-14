package com.ustadmobile.nanolrs.android.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.ormlite.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.model.XapiAgentEntity;
import com.ustadmobile.nanolrs.ormlite.model.XapiForwardingStatementEntity;
import com.ustadmobile.nanolrs.ormlite.model.XapiStatementEntity;
import com.ustadmobile.nanolrs.ormlite.model.XapiVerbEntity;

import java.sql.SQLException;

/**
 * Created by mike on 9/6/16.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {


    private static final String DATABASE_NAME="nanolrs.db";

    private static final int DATABASE_VERSION = 9;

    private Context context;

    private Dao<XapiStatementEntity, Integer> mXapiStatementDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    public static Class[] TABLE_CLASSES = new Class[]{ XapiActivityEntity.class, XapiAgentEntity.class,
            XapiStatementEntity.class, XapiVerbEntity.class};

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            for(Class clazz : TABLE_CLASSES) {
                TableUtils.createTable(connectionSource, clazz);
            }

            TableUtils.createTable(connectionSource, XapiForwardingStatementEntity.class);
        }catch(SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "can't create database", e);
            throw new RuntimeException(e);
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        /*
        try {
            if(oldVersion <= 9) {

            }


            for(Class clazz : TABLE_CLASSES) {
                TableUtils.dropTable(connectionSource, clazz, false);
            }

            if(oldVersion >= 3) {
                TableUtils.dropTable(connectionSource, XapiForwardingStatementEntity.class, false);
            }

            onCreate(database, connectionSource);
        }catch(SQLException e) {
            Log.i(DatabaseHelper.class.getName(), "exception onUpgrade", e);
            throw new RuntimeException(e);
        }
        */
    }






}
