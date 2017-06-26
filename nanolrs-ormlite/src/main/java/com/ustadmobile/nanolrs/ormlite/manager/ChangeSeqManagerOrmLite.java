package com.ustadmobile.nanolrs.ormlite.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.j256.ormlite.dao.Dao;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.model.ChangeSeq;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.ChangeSeqEntity;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        long nextChangeSeq = 0;
        Dao thisDao =
                persistenceManager.getDao(getEntityImplementationClasss(), dbContext);

        Map<String, Object> search = new HashMap<String, Object>();
        search.put("table", tableName);

        List<NanoLrsModel> l = thisDao.queryForFieldValues(search);
        if(l.size()>0){
            ChangeSeq changeSeqEntry = (ChangeSeq)l.get(0);
            nextChangeSeq = changeSeqEntry.getNextChangeSeqNum();
        }else{
            //The entry for the table doesn't exists (maybe its not created yet)
            return 0;
        }

        return nextChangeSeq;
    }

    /**
     * Updates the nextChange Seq Number + increment for ChangeSeq table
     * @param tableName
     * @param increment
     * @param dbContext
     * @throws SQLException
     */
    @Override
    public void getNextChangeAddSeqByTableName(String tableName, int increment,
                                               Object dbContext) throws SQLException {
        Dao thisDao =
                persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        long currentSeqNumber = getNextChangeByTableName(tableName, dbContext);
        long newSeqNumber = currentSeqNumber + increment;

        Map<String, Object> search = new HashMap<String, Object>();
        search.put("table", tableName);
        List<NanoLrsModel> l = thisDao.queryForFieldValues(search);
        if(l.size()>0){
            ChangeSeq changeSeqEntry = (ChangeSeq)l.get(0);
            changeSeqEntry.setNextChangeSeqNum(newSeqNumber);
            thisDao.update(changeSeqEntry);
        }else{
            //Doesn't exist (not created)
            ChangeSeqManager changeSeqManager =
                    persistenceManager.getManager(ChangeSeqManager.class);
            ChangeSeq newChangeSeq = (ChangeSeq) changeSeqManager.makeNew();
            newChangeSeq.setTable(tableName);
            newChangeSeq.setNextChangeSeqNum(increment);
            changeSeqManager.persist(dbContext, newChangeSeq, changeSeqManager);
        }
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user) {
        //TODO: This
        return null;
    }


}
