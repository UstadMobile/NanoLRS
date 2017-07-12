package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.ThisNodeManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.ThisNode;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/21/2017.
 */

public abstract class BaseManagerOrmLiteSyncable<T extends NanoLrsModelSyncable, P>
        extends BaseManagerOrmLite implements NanoLrsManagerSyncable<T,P> {

    @Override
    public List<NanoLrsModel> getAllSinceSequenceNumber(
            User user, Object dbContext, String host, long seqNum) throws SQLException {

        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);

        //Step 1: select sent_sequence from sync_status where host=host,table=tableName;
        //  If nothing exists, sent_sequence = 0;
        //Step 2: select * from tableName where
        // local_sequence/master_sequence > sent_sequence;
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

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException{
        persist(dbContext, data, true);
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data, boolean incrementChangeSeq)
            throws SQLException {
        NanoLrsModelSyncable dataS = (NanoLrsModelSyncable)data;

        if(incrementChangeSeq == true) {
            Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
            String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();
            ChangeSeqManager changeSeqManager =
                    PersistenceManager.getInstance().getManager(ChangeSeqManager.class);
            long setThis = changeSeqManager.getNextChangeAddSeqByTableName(tableName, 1, dbContext);
            dataS.setLocalSequence(setThis);
            /*
            For Master Server
             */
            ThisNodeManager thisNodeManager =
                    PersistenceManager.getInstance().getManager(ThisNodeManager.class);
            //TODO: Get this ID either as final or get All.get(0)
            ThisNode thisNode = (ThisNode) thisNodeManager.findByPrimaryKey(dbContext, "this_device");
            if (thisNode != null) {
                if (thisNode.isMaster()) {
                    dataS.setMasterSequence(setThis);
                }
            }
        }

        super.persist(dbContext, dataS);
    }


    @Override
    public long getLatestMasterSequence(Object dbContext) throws SQLException {
        //TODO:
        return 42;
    }

    @Override
    public NanoLrsModelSyncable findAllRelatedToUser(Object dbContext, User user) {
        //TODO:
        return null;
    }

}
