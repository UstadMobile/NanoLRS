package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.manager.NodeSyncStatusManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.NodeSyncStatus;
import com.ustadmobile.nanolrs.ormlite.generated.model.NodeSyncStatusEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * Ormlite's implementeattion of NodeSyncStatusManager.
 * Created by varuna on 12/17/2017.
 */
public class NodeSyncStatusManagerOrmLite extends BaseManagerOrmLite implements NodeSyncStatusManager {
    @Override
    public List<NodeSyncStatus> getStatusesByNode(Object dbContext, Node node) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeSyncStatusEntity.class, dbContext);
        QueryBuilder<NodeSyncStatusEntity, String> qb = thisDao.queryBuilder();
        qb.orderBy(NodeSyncStatusEntity.COLNAME_SYNC_DATE, false);
        List allStatuses = thisDao.query(qb.where().eq(NodeSyncStatusEntity.COLNAME_NODE, node).prepare());
        return allStatuses;
    }

    @Override
    public NodeSyncStatus getLatestStatusByNode(Object dbContext, Node node) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeSyncStatusEntity.class, dbContext);
        QueryBuilder<NodeSyncStatusEntity, String> qb = thisDao.queryBuilder();
        qb.where().eq(NodeSyncStatusEntity.COLNAME_NODE, node);
        qb.orderBy(NodeSyncStatusEntity.COLNAME_SYNC_DATE, false);
        qb.limit(1L);
        List<NodeSyncStatus> latestRecord = thisDao.query(qb.prepare());
        return latestRecord.get(0);
    }

    @Override
    public NodeSyncStatus getLatestSuccessfulStatusByNode(Object dbContext, Node node)
            throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeSyncStatusEntity.class, dbContext);
        QueryBuilder<NodeSyncStatusEntity, String> qb = thisDao.queryBuilder();
        qb.where().eq(NodeSyncStatusEntity.COLNAME_NODE, node).and().eq(
                NodeSyncStatusEntity.COLNAME_SYNC_RESULT, "200");
        qb.orderBy(NodeSyncStatusEntity.COLNAME_SYNC_DATE, false);
        qb.limit(1L);
        List<NodeSyncStatus> allSuccessStatuses =
                thisDao.query(qb.where().eq(NodeSyncStatusEntity.COLNAME_NODE, node).prepare());
        if(allSuccessStatuses.isEmpty()){
            return null;
        }else{
            return allSuccessStatuses.get(0);
        }
    }

    @Override
    public List<NodeSyncStatus> getSuccessfulStatusesByNode(Object dbContext, Node node)
            throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeSyncStatusEntity.class, dbContext);
        QueryBuilder<NodeSyncStatusEntity, String> qb = thisDao.queryBuilder();
        qb.where().eq(NodeSyncStatusEntity.COLNAME_NODE, node).and().eq(
                NodeSyncStatusEntity.COLNAME_SYNC_RESULT, "200");
        qb.orderBy(NodeSyncStatusEntity.COLNAME_SYNC_DATE, false);
        List<NodeSyncStatus> allSuccessStatuses =
                thisDao.query(qb.where().eq(NodeSyncStatusEntity.COLNAME_NODE, node).prepare());
        if(allSuccessStatuses.isEmpty()){
            return null;
        }else{
            return allSuccessStatuses;
        }
    }

    @Override
    public Class getEntityImplementationClasss() {
        return NodeSyncStatusEntity.class;
    }

}
