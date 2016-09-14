package com.ustadmobile.nanolrs.android.persistence;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Created by mike on 9/14/16.
 */
public interface DatabaseCreateOrUpdateListener {

    /**
     * Run when the database is created.  Can be used to add extra tables to the same database
     *
     * @param database
     * @param connectionSource
     */
    void onDatabaseCreate(SQLiteDatabase database, ConnectionSource connectionSource);


    /**
     * Run when the database is upgraded.  Can be used to add extra tables to the same database
     *
     * @param database
     * @param connectionSource
     * @param oldVersion
     * @param newVersion
     */
    void onDatabaseUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion);



}
