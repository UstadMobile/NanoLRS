package com.ustadmobile.nanolrs.ormlite.manager;

import com.ustadmobile.nanolrs.core.manager.RelationshipTestManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.RelationshipTest;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.RelationshipTestEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/12/2017.
 */

public class RelationshipTestManagerOrmLite extends BaseManagerOrmLite implements RelationshipTestManager {

    //Here we override methods in RelationshipTestManager and BaseManagerOrmLite (that extends from NanoLrsManager)

    //BaseManagerOrmLite:

    @Override
    public Class getEntityImplementationClasss() {
        return RelationshipTestEntity.class;
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user) {
        //TODO: this
        return null;
    }

    //Constructor
    public RelationshipTestManagerOrmLite() {

    }

    //NanoLrsManager (via BaseManagerOrmLite) :

    @Override
    public NanoLrsModel makeNew() {
        return super.makeNew();
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException {
        super.persist(dbContext, data);
    }

    @Override
    public void delete(Object dbContext, NanoLrsModel data) throws SQLException {
        super.delete(dbContext, data);
    }

    @Override
    public NanoLrsModel findByPrimaryKey(Object dbContext, Object primaryKey) throws SQLException {
        return super.findByPrimaryKey(dbContext, primaryKey);
    }

    //RelationshipTestManager (NanoLrsManagerSyncable + RelationshipTestManager):
    @Override
    public List findBySequenceNumber(XapiUser user, Object dbContext, String seqNum) {
        return null;
    }

    @Override
    public List getAllSinceSequenceNumber(XapiUser user, Object dbContext, String seqNum) {
        return null;
    }

}
