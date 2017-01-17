package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.manager.XapiUserManager;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiUserEntity;
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
    public XapiUser createSync(Object dbContext, String id) {
        XapiUserEntity created = new XapiUserEntity();
        created.setUuid(id);
        return created;
    }

    @Override
    public void persist(Object dbContext, XapiUser user) {
        try {
            Dao<XapiUserEntity, String> dao = persistenceManager.getDao(XapiUserEntity.class, dbContext);
            dao.createOrUpdate((XapiUserEntity)user);
        }catch(SQLException e) {
            System.err.println("Exception persist");
            e.printStackTrace();
        }

    }


    @Override
    public XapiUser findById(Object dbContext, String id) {
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
    public List<XapiUser> findByUsername(Object dbContext, String username) {
        try {
            Dao<XapiUserEntity, String> dao = persistenceManager.getDao(XapiUserEntity.class, dbContext);
            QueryBuilder<XapiUserEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(XapiUserEntity.COLNAME_USERNAME, username);
            return (List<XapiUser>)(Object)dao.query(queryBuilder.prepare());
        }catch(Exception e) {
            System.err.println("Exception findByUsername");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void delete(Object dbContext, XapiUser data) {
        try {
            Dao<XapiUserEntity, String> dao = persistenceManager.getDao(XapiUserEntity.class, dbContext);
            dao.delete((XapiUserEntity)data);
        }catch(SQLException e) {
            System.err.println("exception deleting");
            e.printStackTrace();
        }
    }
}
