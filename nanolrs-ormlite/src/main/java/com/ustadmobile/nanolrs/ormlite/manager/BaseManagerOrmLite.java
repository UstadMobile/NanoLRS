package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 10/2/16.
 */

public abstract class BaseManagerOrmLite<T extends NanoLrsModel, P> implements NanoLrsManager<T,P> {

    protected PersistenceManagerORMLite persistenceManager;

    public BaseManagerOrmLite() {

    }

    /**
     * This must return the class that is used as the implementation of the entity proxy interface
     *
     * e.g. for XapiStatementManagerOrmLite it should return XapiStatementEntity.class
     *
     * @return Entity implementation class as above
     */
    public abstract Class getEntityImplementationClasss() ;

    //It used to be: public T makeNew(Object primaryKey) {
    @Override
    public T makeNew() {
        try {
            return (T)getEntityImplementationClasss().newInstance();
        }catch(InstantiationException e) {
            throw new RuntimeException(e);
        }catch(IllegalAccessException e2) {
            throw new RuntimeException(e2);
        }
    }

    @Override
    public void persist(Object dbContext, T data) throws SQLException {
        //always update stored date
        data.setStoredDate(System.currentTimeMillis());

        persistenceManager.getDao(getEntityImplementationClasss(),
                dbContext).createOrUpdate(data);
    }

    @Override
    public void delete(Object dbContext, T data) throws SQLException {
        persistenceManager.getDao(getEntityImplementationClasss(), dbContext).delete(data);
    }

    @Override
    public T findByPrimaryKey(Object dbContext, P primaryKey) throws SQLException {
        return (T)persistenceManager.getDao(getEntityImplementationClasss(), dbContext).queryForId(primaryKey);
    }

    @Override
    public List<T> getAllEntities(Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        QueryBuilder<T, String> qb = thisDao.queryBuilder();
        List<T> allEntities = thisDao.query(qb.prepare());
        return allEntities;
    }


    public PersistenceManagerORMLite getPersistenceManager() {
        return persistenceManager;
    }

    public void setPersistenceManager(PersistenceManagerORMLite persistenceManager) {
        this.persistenceManager = persistenceManager;
    }
}
