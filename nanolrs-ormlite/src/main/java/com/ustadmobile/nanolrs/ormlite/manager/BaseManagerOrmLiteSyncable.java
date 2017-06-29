package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.NanoLrsManagerSyncable;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

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
        /*
        long currentTableMaxSequence = thisDao.queryRawValue(
                thisDao.queryBuilder().selectRaw(
                        "MAX(\"local_sequence\")").prepareStatementString());
        dataS.setLocalSequence(currentTableMaxSequence + 1);
        */
        ///*

        String tableName = ((BaseDaoImpl)thisDao).getTableInfo().getTableName();
        ChangeSeqManager changeSeqManager =
                PersistenceManager.getInstance().getManager(ChangeSeqManager.class);
        //long setThis = changeSeqManager.getNextChangeByTableName(tableName, dbContext);
        long setThis = changeSeqManager.getNextChangeAddSeqByTableName(tableName, 1, dbContext);
        dataS.setLocalSequence(setThis);
        ///*

        super.persist(dbContext, dataS);
    }

    @Override
    public long getLatestMasterSequence(Object dbContext) throws SQLException {
        //TODO:
        return 42;
    }

    public abstract T findAllRelatedToUser(Object dbContext, XapiUser user);

}
