package com.ustadmobile.nanolrs.android.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.nanolrs.ormlite.generated.model.UserEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiAgentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiDocumentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiForwardingStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStateEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiVerbEntity;

import java.sql.SQLException;

/**
 * Created by mike on 9/6/16.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {


    public static final String LOGTAG = "NanoLRS/DatabaseHelper";

    /**
     * Database Name to be used: nanolrs.db was used by previous versions.  From DATABASE_VERSION 11
     */
    private static final String DATABASE_NAME="nanolrs3.db";

    private static final int DATABASE_VERSION = 12;

    private Context context;

    private Dao<XapiStatementEntity, Integer> mXapiStatementDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    public static Class[] TABLE_CLASSES = new Class[]{ XapiActivityEntity.class, XapiAgentEntity.class,
            XapiStatementEntity.class, XapiVerbEntity.class, XapiForwardingStatementEntity.class,
            UserEntity.class, XapiDocumentEntity.class, XapiStateEntity.class
    };

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            for(Class clazz : TABLE_CLASSES) {
                TableUtils.createTable(connectionSource, clazz);
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
}
