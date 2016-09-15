package com.ustadmobile.nanolrs.android.persistence;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mike on 9/6/16.
 */
public class PersistenceManagerAndroid extends PersistenceManagerORMLite {

    private HashMap<Context, DatabaseHelper> helpersMap;

    private Dao<XapiStatementManager, String> xapiStatementDao;

    private List<DatabaseCreateOrUpdateListener> createOrUpdateListeners;


    public static PersistenceManagerAndroid getInstanceAndroid() {
        return (PersistenceManagerAndroid) getInstance();
    }


    public PersistenceManagerAndroid() {
        helpersMap = new HashMap<>();
        createOrUpdateListeners = new ArrayList<>();
    }




    /**
     * Add a Create or Update Listener : use this BEFORE any database operation
     * @param listener
     */
    public void addDatabaseCreateOrUpdateListener(DatabaseCreateOrUpdateListener listener) {
        createOrUpdateListeners.add(listener);
    }

    public void removeDatabaseCreateOrUpdateListener(DatabaseCreateOrUpdateListener listener) {
        createOrUpdateListeners.remove(listener);
    }


    public DatabaseHelper getHelperForContext(Context context) {
        DatabaseHelper helper = helpersMap.get(context);
        if(helper == null) {
            helper = new DatabaseHelper(context);
            helpersMap.put(context, helper);
        }

        return helper;
    }

    public void releaseHelperForContext(Context context) {
        if(helpersMap.containsKey(context)) {
            helpersMap.get(context).close();
            helpersMap.remove(context);
        }
    }

    @Override
    public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz, Object dbContext) throws SQLException {
        return getHelperForContext((Context)dbContext).getDao(clazz);
    }
}
