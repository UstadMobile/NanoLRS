package com.ustadmobile.nanolrs.ormlite.manager;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.lang.reflect.Method;
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
            /*
            Method primaryKeyMethod;
            T created = (T)getEntityImplementationClasss().newInstance();
            //Find the Primary Key from annotation:
            Method[] allMethods = created.getClass().getMethods();
            for(Method relatedMethod : allMethods) {
                if(relatedMethod.isAnnotationPresent(PrimaryKeyAnnotationClass.class)) {
                    primaryKeyMethod = relatedMethod;
                    break;
                }
            }
            return created;
            */
        }catch(InstantiationException e) {
            throw new RuntimeException(e);
        }catch(IllegalAccessException e2) {
            throw new RuntimeException(e2);
        }
    }

    @Override
    public void persist(Object dbContext, T data) throws SQLException {
        //Updating sequence number:
        //TODO: Lookup Master Sequence nad local sequence from another table (max value)
        // or just gt latest of this table, etc
        long currentTableMaxSequence = data.getLocalSequence();
        //currentTableMaxSequence =
        data.setLocalSequence(currentTableMaxSequence + 1);
        //The Sync API will set this, not here. Q: How does one know its a server/client/mini server ?
        //data.setMasterSequence(data.getMasterSequence() + 1);
        persistenceManager.getDao(getEntityImplementationClasss(), dbContext).createOrUpdate(data);
    }

    @Override
    public long getLatestLocalSequence(Object dbContext) throws SQLException {
        //TODO:
        return 42;
    }

    @Override
    public long getLatestMasterSequence(Object dbContext) throws SQLException {
        //TODO:
        return 42;
    }

    @Override
    public void persist(Object dbContext, T data, NanoLrsManager manager) throws SQLException {
        long currentTableMaxSequence = manager.getLatestLocalSequence(dbContext);
        data.setLocalSequence(currentTableMaxSequence + 1);

        persistenceManager.getDao(getEntityImplementationClasss(), dbContext).createOrUpdate(data);
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
