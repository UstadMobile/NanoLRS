package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by varuna on 6/21/2017.
 */

public abstract class BaseManagerOrmLiteSyncable<T extends NanoLrsModelSyncable, P>
        extends BaseManagerOrmLite implements NanoLrsManagerSyncable<T,P> {

    public static String convertCamelCaseNameToUnderscored(String propertyName) {
        String undererScoredName = "";
        for(int i = 0; i < propertyName.length(); i++) {
            if(Character.isUpperCase(propertyName.charAt(i)) && (i == 0 || Character.isLowerCase(propertyName.charAt(i-1)))) {
                undererScoredName += "_";
            }
            undererScoredName += Character.toLowerCase(propertyName.charAt(i));
        }

        return undererScoredName;
    }

    public String getPrimaryKeyFromEntity(Class syncableEntity){
        Class syncableProxy = syncableEntity.getInterfaces()[0];

        //Get the primary key
        Method[] allEntityMethods = syncableProxy.getMethods();
        String pkMethod = null;
        String pkField = null;
        for(Method method : allEntityMethods) {
            if(method.isAnnotationPresent(PrimaryKeyAnnotationClass.class)) {
                pkMethod = method.getName();
                break;
            }
        }
        if(pkMethod == null){
            pkField = "uuid";
        }else{
            int prefixLen = 0;
            if(pkMethod.startsWith("is"))
                prefixLen = 2;
            else if(pkMethod.startsWith("get"))
                prefixLen = 3;
            pkField = Character.toLowerCase(pkMethod.charAt(3)) +
                    pkMethod.substring(prefixLen+1);

        }

        return convertCamelCaseNameToUnderscored(pkField);

    }

    @Override
    public List<NanoLrsModel> getAllSinceSequenceNumber(
            User user, Object dbContext, String host, long seqNum) throws SQLException {
        return getAllSinceTwoSequenceNumber(user, host, seqNum, -1, dbContext);
    }

    @Override
    public List<NanoLrsModel> getAllSinceTwoSequenceNumber(User user, String host,
        long fromSeqNum, long toSeqNum, Object dbContext) throws SQLException {

        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);

        //Get user speific uuids
        List<String> uuidList = new ArrayList<>();
        List<NanoLrsModel> blank = new ArrayList<>();
        if(user == null){
            return blank;
        }
        //Get user's specific subQuery:
        PreparedQuery<NanoLrsModel> subQueryPQ = findAllRelatedToUserQuery(dbContext, user);
        if(subQueryPQ == null){
            return blank;
        }
        //Get list of uuids from subQuery
        List<String[]> subQueryColResultSingle =
                thisDao.queryRaw(subQueryPQ.getStatement()).getResults();
        for(String[] thisEntry:subQueryColResultSingle){
            if(thisEntry[0] != null) {
                uuidList.add(thisEntry[0]);
            }
        }
        if(uuidList.isEmpty()){
            return blank;
        }

        String pkField = getPrimaryKeyFromEntity(getEntityImplementationClasss());

        //Basically searching for new entries that have never been synced with main server
        // between two
        QueryBuilder<NanoLrsModel, String> qbIfMasterSeqNull = thisDao.queryBuilder();
        Where whereMasterSeqNullAndCSGTSN = qbIfMasterSeqNull.where();
        Long to = toSeqNum;
        //whereMasterSeqNullAndCSGTSN.eq("master_sequence", 0);
        //Updated to reflect master seq as -1 for new user registrations
        whereMasterSeqNullAndCSGTSN.lt("master_sequence", 1);
        if(toSeqNum == 0 && fromSeqNum ==0){
            whereMasterSeqNullAndCSGTSN.and()
                    .gt("local_sequence", fromSeqNum);
        }else if(to != -1){
            whereMasterSeqNullAndCSGTSN.and()
                    .gt("local_sequence", fromSeqNum).and().lt("local_sequence", toSeqNum);
        }else{
            whereMasterSeqNullAndCSGTSN.and()
                    .gt("local_sequence", fromSeqNum);
        }

        whereMasterSeqNullAndCSGTSN.and().in(pkField, uuidList);
        PreparedQuery<NanoLrsModel> getAllWhereMSNullAndCSGTSN =
                qbIfMasterSeqNull.prepare();
        List<NanoLrsModel> foundAllWhereMSNullAndCSGTSN =
                thisDao.query(getAllWhereMSNullAndCSGTSN);


        //If no new entities that have updates
        if(foundAllWhereMSNullAndCSGTSN.isEmpty()) {
            QueryBuilder<NanoLrsModel, String> qb = thisDao.queryBuilder();
            Where whereNotSent = qb.where();
            whereNotSent.gt("master_sequence", fromSeqNum);
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
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();
        tableName = tableName.toUpperCase(); //always uppercase

        //set date created if does not exist
        Long dateCreated = dataS.getDateCreated();
        if(dateCreated == null || dataS.getDateCreated() < 1){
            dataS.setDateCreated(System.currentTimeMillis());
        }

        NodeManager nodeManager =
                PersistenceManager.getInstance().getManager(NodeManager.class);
        Node thisNode = nodeManager.getThisNode(dbContext);
        long setLocalSeq = dataS.getLocalSequence();
        long nextChangeSeq = -1;
        if(Long.valueOf(setLocalSeq) != null && setLocalSeq > -1){
            nextChangeSeq = setLocalSeq + 1;
        }

        if(incrementChangeSeq) {
            long setThis = changeSeqManager.getNextChangeAddSeqByTableName(tableName, 1, dbContext);
            dataS.setLocalSequence(setThis);

            /*
            If master, set master seq same as local seq
             */
            if (thisNode != null) {
                if (thisNode.isMaster()) {
                    dataS.setMasterSequence(setThis);
                }else{
                    //only for users, but if not synced yet, we maintain master as -1
                    //this ensures that is new header is set to true.
                    //Its the syncs responsibiliy to change this.
                    //we just maintain its value here while user is updated but still not
                    //synced with master/created.
                    //TODO Check this. Might be redundant
                    if (dataS.getMasterSequence() != -1) {
                        dataS.setMasterSequence(0);
                    }
                    //dataS.setMasterSequence(0);
                }
            }
        }else if(nextChangeSeq > 0){
            //Update the next change seq num in Change Seq by the local seq  + 1
            changeSeqManager.setNextChangeSeqNumByTableName(tableName, nextChangeSeq, dbContext);
        }

        super.persist(dbContext, dataS);
    }


    @Override
    public long getLatestMasterSequence(Object dbContext) throws SQLException {
        //TODO:
        return 42;
    }

    //TODO: Think of adding these in
    /*
    public abstract List<T> findAllViewableByUser(Object dbContext, User user);

    public abstract List<T> findAllViewableByUserSinceSeq(Object dbContext, User user, long sinceSeq);

    public abstract List<T> findAllEditableByUser(Object dbContext, User user);

    */

    public abstract List<NanoLrsModel> findAllRelatedToUser(Object dbContext, User user) throws SQLException;

    public abstract PreparedQuery<NanoLrsModel> findAllRelatedToUserQuery(Object dbContext, User user)
            throws SQLException;

}
