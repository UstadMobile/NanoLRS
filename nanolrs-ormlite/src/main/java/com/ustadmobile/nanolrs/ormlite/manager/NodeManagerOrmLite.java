package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
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
    public List<Node> getNodesByRoleName(Object dbContext, String role_name) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        QueryBuilder<NodeEntity, String> qb = thisDao.queryBuilder();
        List allNodes = thisDao.query(qb.where().eq(NodeEntity.COLNAME_ROLE, role_name).prepare());
        return allNodes;
    }

    ///Proxy
    @Override
    public List<Node> getProxys(Object dbContext) throws SQLException{
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        List<Node> proxyNodes = getNodesByRoleName(dbContext, "proxy");
        if(proxyNodes != null && !proxyNodes.isEmpty()) {
            return proxyNodes;
        }else{
            return null;
        }
    }

    ///Client
    @Override
    public List<Node> getClients(Object dbContext) throws SQLException{
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        List<Node> clientNodes = getNodesByRoleName(dbContext, "client");
        if(clientNodes != null && !clientNodes.isEmpty()) {
            return clientNodes;
        }else{
            return null;
        }
    }

    ///This Device's node
    @Override
    public Node getThisNode(Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        List<Node> thisNodes = getNodesByRoleName(dbContext, "this_node");
        if(thisNodes != null && !thisNodes.isEmpty()) {
            return thisNodes.get(0);
        }else{
            return null;
        }
    }
    @Override
    public Node createThisDeviceNode(String uuid, String thisNodeName, String thisHostName,
                                     String endpointUrl, boolean isMaster, boolean isProxy,
                                     Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        Node thisNode = getThisNode(dbContext);
        if(thisNode == null){
            thisNode = (Node)makeNew();
            thisNode.setUrl(endpointUrl);
            thisNode.setUUID(uuid);
            thisNode.setName(thisNodeName);
            thisNode.setStoredDate(System.currentTimeMillis());
            thisNode.setHost(thisHostName);
            thisNode.setNotes("this_node");
            //*Role is always local.*
            thisNode.setRole("this_node");

            thisNode.setMaster(isMaster);
            thisNode.setProxy(isProxy);

            thisDao.createOrUpdate(thisNode);
        }

        if(thisNode.getHost() == null || thisNode.getHost().startsWith("host:")){
            thisNode.setHost(thisHostName);
            thisDao.createOrUpdate(thisNode);
            thisNode = getThisNode(dbContext);
        }
        return thisNode;
    }

    ///Get Main Node:
    @Override
    public List<Node> getAllMainNodes(Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        QueryBuilder<NodeEntity, String> qb = thisDao.queryBuilder();
        List<Node> allMainNodes = thisDao.query(qb.where().eq(NodeEntity.COLNAME_MASTER, true).prepare());
        if(allMainNodes!=null && !allMainNodes.isEmpty()){
            return allMainNodes;
        }else{
            return null;
        }

    }
    @Override
    public Node getMainNode(String host_name, Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        QueryBuilder<NodeEntity, String> qb3 = thisDao.queryBuilder();
        Where where3 = qb3.where();
        where3.eq(NodeEntity.COLNAME_MASTER, true).and().eq(NodeEntity.COLNAME_HOST, host_name);
        PreparedQuery pq3 = qb3.prepare();

        List<Node> allMainNodes = thisDao.query(pq3);
        if(allMainNodes!=null && !allMainNodes.isEmpty()){
            return allMainNodes.get(0);
        }else{
            return null;
        }
    }
    @Override
    public boolean doesThisMainNodeExist(String host_name, Object dbContext) throws SQLException {
        Node mainNode = getMainNode(host_name, dbContext);
        if(mainNode!= null) {
            if (mainNode.isMaster()) {
                return true;
            }
        }
        return false;
    }


}
