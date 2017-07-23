package com.ustadmobile.nanolrs.ormlite.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.SyncStatus;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.ormlite.generated.model.SyncStatusEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.UserEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SyncStatusManagerOrmLite extends BaseManagerOrmLite implements SyncStatusManager {

    //Constructor
    public SyncStatusManagerOrmLite() {
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException {
        super.persist(dbContext, data);
    }

    @Override
    public Class getEntityImplementationClasss() {
        return SyncStatusEntity.class;
    }

    /**
     * Converts a property name from e.g. from fullName to full_name
     *
     * @param propertyName Property Name e.g. propertyName
     *
     * @return Property named in lower case separated by underscores e.g. property_name
     */
    public String convertCamelCaseNameToUnderscored(String propertyName) {
        String undererScoredName = "";
        for(int i = 0; i < propertyName.length(); i++) {
            if(Character.isUpperCase(propertyName.charAt(i)) && (i == 0 || Character.isLowerCase(propertyName.charAt(i-1)))) {
                undererScoredName += "_";
            }
            undererScoredName += Character.toLowerCase(propertyName.charAt(i));
        }

        return undererScoredName;
    }

    @Override
    public long getSentStatus(String host, Class entity, Object dbContext) throws SQLException {
        //TODO: Can this go in core?
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        String table_name = convertCamelCaseNameToUnderscored(
                Character.toLowerCase(entity.getSimpleName().charAt(0)) +
                        entity.getSimpleName().substring(1));

        QueryBuilder<NanoLrsModel, String> qb = thisDao.queryBuilder();
        Where whereHostTableIs = qb.where();
        whereHostTableIs.eq(SyncStatusEntity.COLNAME_HOST, host)
                .and().eq(SyncStatusEntity.COLNAME_TABLE, table_name);
        PreparedQuery<NanoLrsModel> getAllForHostQuery = qb.prepare();
        List<SyncStatus> syncStatusesForHost = thisDao.query(getAllForHostQuery);

        if(syncStatusesForHost.isEmpty()){
            /* Create new */
            SyncStatusEntity newHostForEntity = (SyncStatusEntity) makeNew();
            newHostForEntity.setTable(table_name);
            newHostForEntity.setHost(host);
            newHostForEntity.setNotes("First sync");
            newHostForEntity.setSentSeq(0);
            newHostForEntity.setReceivedSeq(0);
            newHostForEntity.setUUID(UUID.randomUUID().toString());
            thisDao.createOrUpdate(newHostForEntity);
            return 0;
        }else{
            return syncStatusesForHost.get(0).getSentSeq();
        }
    }

    @Override
    public boolean updateSyncStatusSeqNum(String host, Class entity, long newSentSeq,
                                          long newRecSeq, Object dbContext) throws SQLException {

        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        SyncStatus syncStatus = (SyncStatus)getSyncStatus(host, entity, dbContext);
        if(newRecSeq < 0 && newSentSeq > 0){
            syncStatus.setSentSeq(newSentSeq);
            thisDao.createOrUpdate(syncStatus);
        }
        else if(newRecSeq > 0 && newSentSeq < 0){
            syncStatus.setReceivedSeq(newRecSeq);
            thisDao.createOrUpdate(syncStatus);
        }else if(newRecSeq > 0 && newSentSeq > 0){
            syncStatus.setSentSeq(newSentSeq);
            syncStatus.setReceivedSeq(newRecSeq);
            thisDao.createOrUpdate(syncStatus);
        }else{
            return false;
        }
        return true;
    }

    @Override
    public NanoLrsModel getSyncStatus(String host, Class entity, Object dbContext) throws SQLException{
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        String table_name = convertCamelCaseNameToUnderscored(
                Character.toLowerCase(entity.getSimpleName().charAt(0)) +
                        entity.getSimpleName().substring(1));
        QueryBuilder<NanoLrsModel, String> qb = thisDao.queryBuilder();
        Where whereHostTableIs = qb.where();
        whereHostTableIs.eq(SyncStatusEntity.COLNAME_HOST, host)
                .and().eq(SyncStatusEntity.COLNAME_TABLE, table_name);
        PreparedQuery<NanoLrsModel> getAllForHostQuery = qb.prepare();
        List<SyncStatus> syncStatusesForHost = thisDao.query(getAllForHostQuery);

        if(syncStatusesForHost.isEmpty()){
            /* Create new */
            SyncStatusEntity newHostForEntity = (SyncStatusEntity) makeNew();
            newHostForEntity.setTable(table_name);
            newHostForEntity.setHost(host);
            newHostForEntity.setNotes("First sync");
            newHostForEntity.setSentSeq(0);
            newHostForEntity.setReceivedSeq(0);
            newHostForEntity.setUUID(UUID.randomUUID().toString());
            thisDao.createOrUpdate(newHostForEntity);
            return newHostForEntity;
        }else{
            return syncStatusesForHost.get(0);
        }
    }
}
