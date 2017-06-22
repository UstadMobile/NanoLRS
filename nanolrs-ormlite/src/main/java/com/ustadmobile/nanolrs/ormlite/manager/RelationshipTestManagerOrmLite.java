package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.RelationshipTestEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by varuna on 6/12/2017.
 */

/**
 * Here we override methods in RelationshipTestManager and BaseManagerOrmLite (that extends from NanoLrsManager)
 */
public class RelationshipTestManagerOrmLite extends BaseManagerOrmLiteSyncable
        implements RelationshipTestManager {

    //Constructor
    public RelationshipTestManagerOrmLite() {

    }

    @Override
    public Class getEntityImplementationClasss() {
        return RelationshipTestEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user){
        //TODO: this here or super ?
        return null;
    }

    @Override
    public List<NanoLrsModel> getAllSinceSequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException{
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();

        //Step 1: select sent_sequence from sync_status where host=host, table=tableName;
        //  If nothing exists, sent_sequence = 0;
        //Step 2: select * from tableName where local_sequence/master_sequence > sent_sequence;
        //Step 3: that is a List<entities> and we return it.
        //Step 4: Figure out the role of user in this all (TODO)

        QueryBuilder<NanoLrsModel, String> qb = thisDao.queryBuilder();
        Where whereNotSent = qb.where();
        whereNotSent.gt("master_sequence", seqNum);
        PreparedQuery<NanoLrsModel> getAllNewPreparedQuery = qb.prepare();
        List<NanoLrsModel> foundNewEntriesListModel = thisDao.query(getAllNewPreparedQuery);


        if(foundNewEntriesListModel == null || foundNewEntriesListModel.size() == 0){
            try {
                thisDao.closeLastIterator();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QueryBuilder dbAll = thisDao.queryBuilder();
            PreparedQuery<NanoLrsModel> getAllPreparedQuery = dbAll.prepare();
            foundNewEntriesListModel = thisDao.query(getAllPreparedQuery);
        }

        return foundNewEntriesListModel;

    }
}



    /*
    //BaseManagerOrmLite:

    @Override
    public Class getEntityImplementationClasss() {
        return RelationshipTestEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user) {
        //TODO: this
        return null;
    }

    //Constructor
    public RelationshipTestManagerOrmLite() {

    }

    //NanoLrsManager (via BaseManagerOrmLite) :

    @Override
    public NanoLrsModel makeNew() {
        return super.makeNew();
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException {
        super.persist(dbContext, data);
    }

    @Override
    public void delete(Object dbContext, NanoLrsModel data) throws SQLException {
        super.delete(dbContext, data);
    }

    @Override
    public NanoLrsModel findByPrimaryKey(Object dbContext, Object primaryKey) throws SQLException {
        return super.findByPrimaryKey(dbContext, primaryKey);
    }

    //RelationshipTestManager (NanoLrsManagerSyncable + RelationshipTestManager):
    @Override
    public List findBySequenceNumber(XapiUser user, Object dbContext,
                                     String host,  long seqNum) {
        //TODO: this? or do we super it ?
        return null;
    }

    @Override
    public List getAllSinceSequenceNumber(XapiUser user, Object dbContext,
                                          String host, long seqNum) {
        //TODO: this? or do we super it ?
        return null;
    }
    */


