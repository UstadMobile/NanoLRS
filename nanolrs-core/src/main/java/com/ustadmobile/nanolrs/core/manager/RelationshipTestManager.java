package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.XapiUser;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 9/27/16.
 */

public interface RelationshipTestManager extends NanoLrsManagerSyncable{
    //Here we do not override the NanoLrsManager stuff, because BaseManagerOrmLite
    // does that. Here we put in methods specific to this entity: RelationshipTest
    // Also, in here we have methods in addition to the ones in NanoLrsManager, we have
    // the NanoLrsManagerSyncable entities. We Override those so that they get passed
    // on to the Manager implementation : eg: RelationshipTestManagerOrmLite, etc

    @Override
    List findBySequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException;

    @Override
    List<NanoLrsModel> getAllSinceSequenceNumber(
            XapiUser user, Object dbContext, String host, long seqNum) throws SQLException;
}
