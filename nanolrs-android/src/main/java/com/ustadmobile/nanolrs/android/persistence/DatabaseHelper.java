package com.ustadmobile.nanolrs.android.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.nanolrs.ormlite.model.XapiStatementEntity;

import java.sql.SQLException;

/**
 * Created by mike on 9/6/16.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {


    private static final String DATABASE_NAME="lrs.db";

    private static final int DATABASE_VERSION = 1;

    private Context context;

    private Dao<XapiStatementEntity, Integer> mXapiStatementDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, XapiStatementEntity.class);
        }catch(SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "can't create database", e);
            throw new RuntimeException(e);
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource,XapiStatementEntity.class, false);
        }catch(SQLException e) {
            Log.i(DatabaseHelper.class.getName(), "exception onUpgrade", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<XapiStatementEntity, Integer> getXapiStatementDao() throws SQLException {
        if(mXapiStatementDao == null) {
            mXapiStatementDao = getDao(XapiStatementEntity.class);
        }

        return mXapiStatementDao;
    }
}
