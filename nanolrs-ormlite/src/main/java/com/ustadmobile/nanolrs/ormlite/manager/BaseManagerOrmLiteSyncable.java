package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterable;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.Query;


/**
 * Created by varuna on 6/21/2017.
 */

public abstract class BaseManagerOrmLiteSyncable<T extends NanoLrsModelSyncable, P>
        extends BaseManagerOrmLite implements NanoLrsManagerSyncable<T,P> {
    /*
    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) {
        //TODO:
        //This will be very specific to every entity.
        return null;
    }
    */

    @Override
    public List<NanoLrsModel> getAllSinceSequenceNumber(
            User user, Object dbContext, String host, long seqNum) throws SQLException {

        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);

        //Step 1: select sent_sequence from sync_status where host=host,table=tableName;
        //  If nothing exists, sent_sequence = 0;
        //Step 2: select * from tableName where
        // local_sequence/master_sequence > sent_sequence;
        //Step 2b: extra step: If Master Sequence is 0, we compare local
        // sequence numbers so we don't send the same thing back to the
        // node that we got it from.. (proxy, another node) : cause master
        // will be compared with master sequence.
        //Step 3: that is a List<entities> and we return it.
        //Step 4: Figure out the role of user in this all

        List<String> uuidList = new ArrayList<>();
        //Get user's specific subQuery:
        PreparedQuery<NanoLrsModel> subQueryPQ = findAllRelatedToUserQuery(dbContext, user);

        //Get list of uuids from subQuery
        List<String[]> subQueryColResultSingle =
                thisDao.queryRaw(subQueryPQ.getStatement()).getResults();
        for(String[] thisEntry:subQueryColResultSingle){
            uuidList.add(thisEntry[0]);
        }


        QueryBuilder<NanoLrsModel, String> qbIfMasterSeqNull = thisDao.queryBuilder();
        Where whereMasterSeqNullAndCSGTSN = qbIfMasterSeqNull.where();
        whereMasterSeqNullAndCSGTSN.eq("master_sequence", 0).and().gt("local_sequence", seqNum);
        whereMasterSeqNullAndCSGTSN.and().in("uuid", uuidList);
        PreparedQuery<NanoLrsModel> getAllWhereMSNullAndCSGTSN =
                qbIfMasterSeqNull.prepare();
        List<NanoLrsModel> foundAllWhereMSNullAndCSGTSN =
                thisDao.query(getAllWhereMSNullAndCSGTSN);


        if(foundAllWhereMSNullAndCSGTSN.isEmpty()) {
            QueryBuilder<NanoLrsModel, String> qb = thisDao.queryBuilder();
            Where whereNotSent = qb.where();
            whereNotSent.gt("master_sequence", seqNum);
            PreparedQuery<NanoLrsModel> getAllNewPreparedQuery = qb.prepare();
            List<NanoLrsModel> foundNewEntriesListModel = thisDao.query(getAllNewPreparedQuery);

            return foundNewEntriesListModel;
        }else{
            return foundAllWhereMSNullAndCSGTSN;
        }

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
            NodeManager nodeManager =
                    PersistenceManager.getInstance().getManager(NodeManager.class);
            //TODO: Get this ID either as final or get All.get(0)
            Node thisNode = (Node) nodeManager.findByPrimaryKey(dbContext, "this_device");
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

    /*
    @Override
    public List findAllViewableByUser(Object dbContext, User user) {
        //TODO:
        return null;
    }

    @Override
    public List findAllViewableByUserSinceSeq(Object dbContext, User user, long sinceSeq) {
        //TODO:
        return null;
    }

    @Override
    public List findAllEditableByUser(Object dbContext, User user) {
        return null;
    }
    */

    @Override
    public NanoLrsModelSyncable findAllRelatedToUser(Object dbContext, User user) {
        //TODO:
        /*
        So this method is used to find all entites that are related to this user.
        Why would we need to find that ?
        eg: varunasCanViewTheseClasses = clazzManager.findAllRelatedtoUser(context, varuna);
        schoolManagerCanViewTheseClasses = clazzManager.findAllRelatedToUser(context, schoolManager);

        View and Edit are different. So we ideally need two methods : View and Edit ?
         */
        return null;
    }

}
