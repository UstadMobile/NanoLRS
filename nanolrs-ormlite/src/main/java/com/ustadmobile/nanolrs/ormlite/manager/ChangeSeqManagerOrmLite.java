package com.ustadmobile.nanolrs.ormlite.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.model.ChangeSeq;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.ChangeSeqEntity;

import java.sql.SQLException;
import java.util.List;

public class ChangeSeqManagerOrmLite extends BaseManagerOrmLite
        implements ChangeSeqManager {

    //Constructor
    public ChangeSeqManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return ChangeSeqEntity.class;
    }

    /**
     * Gets the nextChangeSeqNumber for table name (string)
     * @param tableName
     * @param dbContext
     * @return
     * @throws SQLException
     */
    @Override
    public long getNextChangeByTableName(String tableName, Object dbContext) throws SQLException {
        Dao thisDao =
                persistenceManager.getDao(getEntityImplementationClasss(), dbContext);

        boolean createNew = false;
        tableName = tableName.toUpperCase();
        QueryBuilder qb = thisDao.queryBuilder();
        Where where = qb.where();
        where.eq(ChangeSeqEntity.COLNAME_TABLE, tableName);
        PreparedQuery pq = qb.prepare();
        List<ChangeSeq> tableChangeSeqs = thisDao.query(pq);

        if (tableChangeSeqs != null && !tableChangeSeqs.isEmpty()) {
            ChangeSeq tableChangeSeq = tableChangeSeqs.get(0);
            if (tableChangeSeq != null) {
                return tableChangeSeq.getNextChangeSeqNum();
            }else{
                createNew = true;
            }
        }else{
            createNew = true;
        }

        if(createNew == true){
            //create a new one
            long newEntryNextChange = 1;
            ChangeSeqManager changeSeqManager =
                    persistenceManager.getManager(ChangeSeqManager.class);
            ChangeSeq newChangeSeq = (ChangeSeq) changeSeqManager.makeNew();
            newChangeSeq.setTable(tableName);
            newChangeSeq.setNextChangeSeqNum(newEntryNextChange);
            thisDao.createOrUpdate(newChangeSeq);
            return newEntryNextChange;
        }
        return 0;
    }

    /**
     * Updates the nextChange Seq Number + increment for ChangeSeq table
     * @param tableName
     * @param increment
     * @param dbContext
     * @throws SQLException
     */
    @Override
    public long getNextChangeAddSeqByTableName(String tableName, int increment,
                                               Object dbContext) throws SQLException {
        Dao thisDao =
                persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        tableName = tableName.toUpperCase();
        long nextChangeSeqNum = getNextChangeByTableName(tableName, dbContext);
        long newNextChangeSeqNumber = nextChangeSeqNum + increment;

        ChangeSeq tableChangeSeq =
                (ChangeSeq) thisDao.queryBuilder().where().eq(ChangeSeqEntity.COLNAME_TABLE,
                        tableName).queryForFirst();
        if (tableChangeSeq != null){
            //return next change seq num and increment it by increment value
            tableChangeSeq.setNextChangeSeqNum(newNextChangeSeqNumber);
            thisDao.update(tableChangeSeq);

            //Update: Returning the next one not the incremented version (unless newly created)
            //return newNextChangeSeqNumber;
            return nextChangeSeqNum;
        }else{
            //Doesn't exist (not created)
            //return the start and increment
            ChangeSeqManager changeSeqManager =
                    persistenceManager.getManager(ChangeSeqManager.class);
            ChangeSeq newChangeSeq = (ChangeSeq) changeSeqManager.makeNew();
            newChangeSeq.setTable(tableName);
            newChangeSeq.setNextChangeSeqNum(increment + 1);
            thisDao.createOrUpdate(newChangeSeq);
            //return increment;
            return 1;
        }
    }

    @Override
    public boolean setNextChangeSeqNumByTableName(String tableName, long nextChangeSeq, Object dbContext) throws SQLException {
        Dao thisDao =
                persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        tableName = tableName.toUpperCase();

        long newSeqNumber = nextChangeSeq;

        ChangeSeq tableChangeSeq =
                (ChangeSeq) thisDao.queryBuilder().where().eq(ChangeSeqEntity.COLNAME_TABLE,
                        tableName).queryForFirst();
        if(nextChangeSeq < 1){
            return false;
        }
        else if (tableChangeSeq != null){
            tableChangeSeq.setNextChangeSeqNum(newSeqNumber);
            thisDao.update(tableChangeSeq);
            return true;

        }else{
            //Doesn't exist (not created)
            ChangeSeqManager changeSeqManager =
                    persistenceManager.getManager(ChangeSeqManager.class);
            ChangeSeq newChangeSeq = (ChangeSeq) changeSeqManager.makeNew();
            newChangeSeq.setTable(tableName);
            newChangeSeq.setNextChangeSeqNum(newSeqNumber);
            thisDao.createOrUpdate(newChangeSeq);
            return true;
        }
    }

}
