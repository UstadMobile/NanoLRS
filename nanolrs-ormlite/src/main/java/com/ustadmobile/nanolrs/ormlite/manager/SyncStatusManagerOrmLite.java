package com.ustadmobile.nanolrs.ormlite.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.SyncStatusEntity;

public class SyncStatusManagerOrmLite extends BaseManagerOrmLite implements SyncStatusManager {

    //Constructor
    public SyncStatusManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return SyncStatusEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user) {
        //TOOD: this
        return null;
    }


}
