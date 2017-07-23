package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.NodeEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by varuna on 7/10/2017.
 */

public class NodeManagerOrmLite extends BaseManagerOrmLite implements NodeManager {
    @Override
    public Class getEntityImplementationClasss() {
        return NodeEntity.class;
    }

    @Override
    public List<Node> getNodeByRoleName(Object dbContext, String role_name) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        QueryBuilder<NodeEntity, String> qb = thisDao.queryBuilder();
        List allNodes = thisDao.query(qb.where().eq(NodeEntity.COLNAME_ROLE, role_name).prepare());
        return allNodes;
    }

    @Override
    public Node getThisNode(Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        List<Node> thisNodes = getNodeByRoleName(dbContext, "this_node");
        if(thisNodes != null && !thisNodes.isEmpty()) {
            return thisNodes.get(0);
        }else{
            return null;
        }

    }

    @Override
    public Node createThisDeviceNode(String uuid, String deviceName, String endpointUrl,
                                     Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        Node thisNode = getThisNode(dbContext);
        if(thisNode == null){
            thisNode = (Node)makeNew();
            thisNode.setUrl(endpointUrl);
            thisNode.setUUID(uuid);
            thisNode.setRole("this_node");
            thisNode.setStoredDate(System.currentTimeMillis());
            thisNode.setHost("this_node");
            thisNode.setName("this_node");
            thisNode.setMaster(false);
            thisNode.setProxy(false);
            thisDao.createOrUpdate(thisNode);
        }
        return thisNode;
    }

    @Override
    public boolean doesThisMainNodeExist(String name, String host_name, Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        QueryBuilder<NodeEntity, String> qb = thisDao.queryBuilder();
        List<Node> allNodes = thisDao.query(qb.where().eq(
                NodeEntity.COLNAME_HOST, host_name
                ).and().eq(NodeEntity.COLNAME_NAME, name
            ).prepare());
        if(allNodes != null && !allNodes.isEmpty()){
            for(Node everynode:allNodes){
                if(everynode.isMaster()){
                    return true;
                }
            }
        }

        return false;
    }


}
