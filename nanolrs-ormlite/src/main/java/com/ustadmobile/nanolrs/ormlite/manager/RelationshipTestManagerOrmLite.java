package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.RelationshipTestEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by varuna on 6/12/2017.
 */

/**
 * Here we override methods in RelationshipTestManager and BaseManagerOrmLite (that extends from NanoLrsManager)
 */
public class RelationshipTestManagerOrmLite extends BaseManagerOrmLiteSyncable
        implements RelationshipTestManager {

    //Constructor
    public RelationshipTestManagerOrmLite() {}

    @Override
    public Class getEntityImplementationClasss() {
        return RelationshipTestEntity.class;
    }

}


