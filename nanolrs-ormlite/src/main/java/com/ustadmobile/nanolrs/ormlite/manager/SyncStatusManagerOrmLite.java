package com.ustadmobile.nanolrs.ormlite.manager;
/**
 * Created by varuna on 6/23/2017.
 */

import com.ustadmobile.nanolrs.core.manager.SyncStatusManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.SyncStatusEntity;

import java.sql.SQLException;

public class SyncStatusManagerOrmLite extends BaseManagerOrmLite implements SyncStatusManager {

    //Constructor
    public SyncStatusManagerOrmLite() {
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException {
        super.persist(dbContext, data);
    }

    @Override
    public Class getEntityImplementationClasss() {
        return SyncStatusEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, User user) {
        //TOOD: this
        return null;
    }
}
