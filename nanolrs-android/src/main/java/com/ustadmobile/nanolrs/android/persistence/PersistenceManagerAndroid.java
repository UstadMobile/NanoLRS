package com.ustadmobile.nanolrs.android.persistence;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.model.XapiStatementManager;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by mike on 9/6/16.
 */
public class PersistenceManagerAndroid extends PersistenceManagerORMLite {

    private HashMap<Context, DatabaseHelper> helpersMap;

    private Dao<XapiStatementManager, String> xapiStatementDao;

    public PersistenceManagerAndroid() {
        helpersMap = new HashMap<>();
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
