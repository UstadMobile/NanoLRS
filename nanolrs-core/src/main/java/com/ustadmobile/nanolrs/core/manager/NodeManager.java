package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 7/10/2017.
 */

import com.ustadmobile.nanolrs.core.model.Node;

import java.sql.SQLException;
import java.util.List;

public interface NodeManager extends NanoLrsManager {

    /**
     * Get all nodes by role name : main, proxy, client
     * @param dbContext
     * @param role_name
     * @return
     * @throws SQLException
     */
    List<Node> getNodesByRoleName(Object dbContext, String role_name)
            throws SQLException;

    /**
     * Returns this device's node
     * @param dbContext
     * @return
     * @throws SQLException
     */
    Node getThisNode(Object dbContext) throws SQLException;

    /**
     * Creates this device's node
     * @param uuid
     * @param deviceName
     * @param endpointUrl
     * @param dbContext
     * @return
     * @throws SQLException
     */
    Node createThisDeviceNode(String uuid, String deviceName, String hostName, String endpointUrl,
                              boolean isMaster, boolean isProxy,
                              Object dbContext) throws SQLException;

    /**
     * gets all main nodes present
     * @param dbContext
     * @return
     * @throws SQLException
     */
    List<Node> getAllMainNodes(Object dbContext) throws SQLException;

    /**
     * Gets the main node associated with given host_name
     * @param host_name
     * @param dbContext
     * @return
     * @throws SQLException
     */
    Node getMainNode(String host_name, Object dbContext) throws SQLException;

    /**
     * Checks if a main node exists with the host name given
     * @param host
     * @param dbContext
     * @return
     * @throws SQLException
     */
    boolean doesThisMainNodeExist(String host, Object dbContext) throws  SQLException;

    /**
     * Gets all proxys available
     * @param dbContext
     * @return
     * @throws SQLException
     */
    List<Node> getProxys(Object dbContext) throws SQLException;

    /**
     * Gets all clients available
     * @param dbContext
     * @return
     * @throws SQLException
     */
    List<Node> getClients(Object dbContext) throws SQLException;

}
