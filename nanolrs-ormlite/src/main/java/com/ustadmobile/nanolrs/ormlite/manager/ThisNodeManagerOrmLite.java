package com.ustadmobile.nanolrs.ormlite.manager;

import com.ustadmobile.nanolrs.core.manager.ThisNodeManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.ThisNodeEntity;

/**
 * Created by varuna on 7/9/2017.
 */

public class ThisNodeManagerOrmLite extends BaseManagerOrmLite implements ThisNodeManager {
    @Override
    public Class getEntityImplementationClasss() {
        return ThisNodeEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, User user) {
        //TODO:
        return null;
    }
}
