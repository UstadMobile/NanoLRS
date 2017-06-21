package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

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
        long currentTableMaxSequence = data.getLocalSequence();
        data.setLocalSequence(currentTableMaxSequence + 1);
        //The Sync API will set this, not here. Q: How does one know its a server/client/mini server ?
        //data.setMasterSequence(data.getMasterSequence() + 1);
        persistenceManager.getDao(getEntityImplementationClasss(), dbContext).createOrUpdate(data);
    }

    @Override
    public long getLatestMasterSequence(Object dbContext) throws SQLException {
        //TODO:
        return 42;
    }

    @Override
    public long getLatestLocalSequence(Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();
        QueryBuilder qb = thisDao.queryBuilder();
        qb.selectRaw("MAX(\"local_sequence\")");
        String rawString = qb.prepareStatementString();

        GenericRawResults results = thisDao.queryRaw(rawString);
        Object result = results.getFirstResult();
        long currentMaxLocalSequence = thisDao.queryRawValue(rawString);
        return currentMaxLocalSequence;

    }

    @Override
    public void persist(Object dbContext, T data, NanoLrsManager manager) throws SQLException {
        long currentTableMaxSequence = manager.getLatestLocalSequence(dbContext);
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);

        //OR (without manager):
        long currentTableMaxSequence2 = thisDao.queryRawValue(
                thisDao.queryBuilder().selectRaw(
                        "MAX(\"local_sequence\")").prepareStatementString());

        data.setLocalSequence(currentTableMaxSequence + 1);


        thisDao.createOrUpdate(data);

        //lets commit after this.. Not sure if autocommmit=true will solve this (TODO: check)
        String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();
        ConnectionSource cs = (ConnectionSource) dbContext;
        DatabaseConnection dc = cs.getReadWriteConnection(tableName);
        thisDao.commit(dc);
    }

    @Override
    public void delete(Object dbContext, T data) throws SQLException {
        persistenceManager.getDao(getEntityImplementationClasss(), dbContext).delete(data);
    }

    @Override
    public T findByPrimaryKey(Object dbContext, P primaryKey) throws SQLException {
        return (T)persistenceManager.getDao(getEntityImplementationClasss(), dbContext).queryForId(primaryKey);
    }

    public abstract T findAllRelatedToUser(Object dbContext, XapiUser user);

    public PersistenceManagerORMLite getPersistenceManager() {
        return persistenceManager;
    }

    public void setPersistenceManager(PersistenceManagerORMLite persistenceManager) {
        this.persistenceManager = persistenceManager;
    }
}
