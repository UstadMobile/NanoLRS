package com.ustadmobile.nanolrs.ormlite.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.model.ChangeSeq;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.ChangeSeqEntity;

import java.sql.SQLException;

public class ChangeSeqManagerOrmLite extends BaseManagerOrmLiteSyncable
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

        try {
            ChangeSeq tableChangeSeq = (ChangeSeq) thisDao.query(thisDao.queryBuilder().where().eq(
                    ChangeSeqEntity.COLNAME_TABLE, tableName).prepare()).get(0);
            if(tableChangeSeq != null){
                return tableChangeSeq.getNextChangeSeqNum();
            }else{
                return 0;
            }
        }catch(IndexOutOfBoundsException ob){
            return 0;
        }

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
        long currentSeqNumber = getNextChangeByTableName(tableName, dbContext);
        long newSeqNumber = currentSeqNumber + increment;

        ChangeSeq tableChangeSeq =
                (ChangeSeq) thisDao.queryBuilder().where().eq(ChangeSeqEntity.COLNAME_TABLE,
                        tableName).queryForFirst();
        if (tableChangeSeq != null){
            tableChangeSeq.setNextChangeSeqNum(newSeqNumber);
            thisDao.update(tableChangeSeq);
            return newSeqNumber;
        }else{
            //Doesn't exist (not created)
            ChangeSeqManager changeSeqManager =
                    persistenceManager.getManager(ChangeSeqManager.class);
            ChangeSeq newChangeSeq = (ChangeSeq) changeSeqManager.makeNew();
            newChangeSeq.setTable(tableName);
            newChangeSeq.setNextChangeSeqNum(increment);
            thisDao.createOrUpdate(newChangeSeq);
            return increment;
        }
    }

    @Override
    public NanoLrsModelSyncable findAllRelatedToUser(Object dbContext, User user) {
        //TODO: This
        return null;
    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) throws SQLException {
        //TODO:
        return null;
    }

}
