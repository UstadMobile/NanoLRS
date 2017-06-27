package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/21/2017.
 */

public abstract class BaseManagerOrmLiteSyncable<T extends NanoLrsModelSyncable, P>
        extends BaseManagerOrmLite implements NanoLrsManagerSyncable<T,P> {

    @Override
    public List<T> findBySequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException {
        return null;
    }

    @Override
    public List<NanoLrsModel> getAllSinceSequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException {

        return null;
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException {
        //Used to be T data
        NanoLrsModelSyncable dataS = (NanoLrsModelSyncable)data;

        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);

        long currentTableMaxSequence = thisDao.queryRawValue(
                thisDao.queryBuilder().selectRaw(
                        "MAX(\"local_sequence\")").prepareStatementString());
        dataS.setLocalSequence(currentTableMaxSequence + 1);

        thisDao.createOrUpdate(dataS);

        //TODO: Maybe move this to super
        //lets commit after this.. Not sure if autocommmit=true will solve this (TODO: check)
        String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();
        ConnectionSource cs = (ConnectionSource) dbContext;
        DatabaseConnection dc = cs.getReadWriteConnection(tableName);
        thisDao.commit(dc);
        //TODO: do we need to super?
    }

    @Override
    public long getLatestMasterSequence(Object dbContext) throws SQLException {
        //TODO:
        return 42;
    }

    @Override
    public long getLatestLocalSequence(Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(getEntityImplementationClasss(), dbContext);
        String tableName = ((BaseDaoImpl) thisDao).getTableInfo().getTableName();
        QueryBuilder qb = thisDao.queryBuilder();
        qb.selectRaw("MAX(\"local_sequence\")");
        String rawString = qb.prepareStatementString();

        long currentMaxLocalSequence = thisDao.queryRawValue(rawString);
        return currentMaxLocalSequence;

    }

    public abstract T findAllRelatedToUser(Object dbContext, XapiUser user);

}
