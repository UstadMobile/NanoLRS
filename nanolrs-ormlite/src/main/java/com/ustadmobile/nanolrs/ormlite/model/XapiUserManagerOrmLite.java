package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.model.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.XapiUserProxy;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 9/27/16.
 */

public class XapiUserManagerOrmLite implements XapiUserManager {

    private PersistenceManagerORMLite persistenceManager;

    public XapiUserManagerOrmLite(PersistenceManagerORMLite persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public XapiUserProxy createSync(Object dbContext, String id) {
        XapiUserEntity created = new XapiUserEntity();
        created.setId(id);
        return created;
    }

    @Override
    public void persist(Object dbContext, XapiUserProxy user) {
        try {
            Dao<XapiUserEntity, String> dao = persistenceManager.getDao(XapiUserEntity.class, dbContext);
            dao.createOrUpdate((XapiUserEntity)user);
        }catch(SQLException e) {
            System.err.println("Exception persist");
            e.printStackTrace();
        }

    }


    @Override
    public XapiUserProxy findById(Object dbContext, String id) {
        try {
            Dao<XapiUserEntity, String> dao = persistenceManager.getDao(XapiUserEntity.class, dbContext);
            return dao.queryForId(id);
        }catch(SQLException e) {
            System.err.println("Exception findById");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XapiUserProxy> findByUsername(Object dbContext, String username) {
        try {
            Dao<XapiUserEntity, String> dao = persistenceManager.getDao(XapiUserEntity.class, dbContext);
            QueryBuilder<XapiUserEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(XapiUserEntity.COLNAME_USERNAME, username);
            return (List<XapiUserProxy>)(Object)dao.query(queryBuilder.prepare());
        }catch(Exception e) {
            System.err.println("Exception findByUsername");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void delete(Object dbContext, XapiUserProxy data) {
        try {
            Dao<XapiUserEntity, String> dao = persistenceManager.getDao(XapiUserEntity.class, dbContext);
            dao.delete((XapiUserEntity)data);
        }catch(SQLException e) {
            System.err.println("exception deleting");
            e.printStackTrace();
        }
    }
}
