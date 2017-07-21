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

/**
 * Created by varuna on 7/10/2017.
 */

public class NodeManagerOrmLite extends BaseManagerOrmLite implements NodeManager {
    @Override
    public Class getEntityImplementationClasss() {
        return NodeEntity.class;
        //return null;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, User user) {
        //TODO: This
        return null;
    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) {
        return null;
    }

    @Override
    public List<Node> getNodeByRoleName(Object dbContext, String role_name) throws SQLException {
        Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);
        QueryBuilder<NodeEntity, String> qb = thisDao.queryBuilder();
        List allNodes = thisDao.query(qb.where().eq(NodeEntity.COLNAME_ROLE, role_name).prepare());
        return allNodes;
    }
}
