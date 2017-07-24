package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 7/10/2017.
 */

import com.ustadmobile.nanolrs.core.model.Node;

import java.sql.SQLException;
import java.util.List;

public interface NodeManager extends NanoLrsManager {

    List<Node> getNodeByRoleName(Object dbContext, String role_name)
            throws SQLException;

    Node getThisNode(Object dbContext) throws SQLException;

    Node createThisDeviceNode(String uuid, String deviceName, String endpointUrl,
                              Object dbContext) throws SQLException;

    List<Node> getMainNodes(Object dbContext) throws SQLException;

    Node getMainNode(String host_name, Object dbContext) throws SQLException;

    boolean doesThisMainNodeExist(String name, String host, Object dbContext) throws  SQLException;
}
