package com.ustadmobile.nanolrs.ormlite.manager;

import com.ustadmobile.nanolrs.core.manager.RelationshipTest2StudentManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;
import com.ustadmobile.nanolrs.ormlite.generated.model.RelationshipTest2StudentEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by varuna on 6/13/2017.
 */

public class RelationshipTest2StudentManagerOrmLite extends BaseManagerOrmLite
        implements RelationshipTest2StudentManager {
    @Override
    public List findBySequenceNumber(XapiUser user, Object dbContext, String host, long seqNum) {
        return null;
    }

    @Override
    public List getAllSinceSequenceNumber(XapiUser user, Object dbContext, String host, long seqNum) {
        return null;
    }

    public RelationshipTest2StudentManagerOrmLite() {
        //super();
    }

    @Override
    public Class getEntityImplementationClasss() {
        return RelationshipTest2StudentEntity.class;
    }

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

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, XapiUser user) {
        return null;
    }
}
