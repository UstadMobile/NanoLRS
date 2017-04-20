package com.ustadmobile.nanolrs.jdbc.persistence;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiAgentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiDocumentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiForwardingStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStateEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiUserEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiVerbEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;
//import com.ustadmobile.nanolrs.buildconfig.TestConstants;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Created by Varuna on 4/4/2017.
 */
public class PersistenceManagerJDBC extends PersistenceManagerORMLite {

    private boolean initRan = false;

    public PersistenceManagerJDBC(){

    }

    @Override
    public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz, Object dbContext) throws SQLException {
        if(!initRan)
            init((ConnectionSource)dbContext);
        return DaoManager.createDao((ConnectionSource)dbContext, clazz);
    }

    public void init(ConnectionSource connectionSource) {
        try {
            System.out.println("onCreate");
            for(Class clazz : TABLE_CLASSES) {
                TableUtils.createTableIfNotExists(connectionSource, clazz);
            }
            initRan = true;
        }catch(SQLException e) {
            System.out.println("can't create database");
            throw new RuntimeException(e);
        }
    }

    public static Class[] TABLE_CLASSES = new Class[]{ XapiActivityEntity.class, XapiAgentEntity.class,
            XapiStatementEntity.class, XapiVerbEntity.class, XapiForwardingStatementEntity.class,
            XapiUserEntity.class, XapiDocumentEntity.class, XapiStateEntity.class
    };


}