package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.stmt.PreparedQuery;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.NodeEntity;

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

}
