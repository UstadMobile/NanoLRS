package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
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
    public List getAllSinceSequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException{
        //TODO: this here. Can make use of super if needed.
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();

        //Step 1: select sent_sequence from sync_status where host=host, table=tableName;
        //  If nothing exists, sent_sequence = 0;
        //Step 2: select * from tableName where local_sequence/master_sequence > sent_sequence;
        //Step 3: that is a List<entities> and we return it.
        //Step 4: Figure out the role of user in this all (TODO)

        long sent_sequence = 0;

        QueryBuilder qb = thisDao.queryBuilder();
        qb.selectRaw("*");
        Where whereNotSent = qb.where();
        whereNotSent.gt("master_sequence", sent_sequence);
        String getAllNewString = qb.prepareStatementString();

        GenericRawResults foundNewEntries = thisDao.queryRaw(getAllNewString);
        Iterator foundNewEntriesIterator = foundNewEntries.iterator();
        List foundNewEntriesResults = foundNewEntries.getResults();

        if(foundNewEntriesIterator== null || foundNewEntriesResults.size() == 0){
            try {
                thisDao.closeLastIterator();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QueryBuilder dbAll = thisDao.queryBuilder();
            dbAll.selectRaw("*");
            String getAllRawString = dbAll.prepareStatementString();

            GenericRawResults allTheEntries = thisDao.queryRaw(getAllRawString);

            foundNewEntriesResults = allTheEntries.getResults();
            if(foundNewEntriesResults.size() >0){
                foundNewEntriesIterator = allTheEntries.iterator();

                int heyFoundSomething = 0;

            }else{
                foundNewEntriesResults = null;
            }
        }

        return foundNewEntriesResults;

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


