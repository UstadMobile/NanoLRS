package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 12/17/2017.
 */

import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.NodeSyncStatus;
import java.sql.SQLException;
import java.util.List;

/**
 * The Manager for NodeSyncStatus
 */
public interface NodeSyncStatusManager extends NanoLrsManager {

    /**
     * Get all statuses by node
     * @param dbContext
     * @param node
     * @return
     * @throws SQLException
     */
    List<NodeSyncStatus> getStatusesByNode(Object dbContext, Node node)
            throws SQLException;

    /**
     * Get latest status by node
     */
    NodeSyncStatus getLatestStatusByNode(Object dbContext, Node node)
        throws SQLException;

    /**
     * Get the latest successful sync status.
     * @param dbContext
     * @param node
     * @return
     * @throws SQLException
     */
    NodeSyncStatus getLatestSuccessfulStatusByNode(Object dbContext, Node node)
        throws SQLException;

    /**
     * Get all successful statuses by  node
     * @param dbContext
     * @param node
     * @return
     * @throws SQLException
     */
    List<NodeSyncStatus> getSuccessfulStatusesByNode(Object dbContext, Node node)
        throws SQLException;

}
