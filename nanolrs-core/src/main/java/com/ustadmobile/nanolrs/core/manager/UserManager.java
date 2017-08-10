package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 9/27/16.
 */

public interface UserManager extends NanoLrsManagerSyncable {

    User createSync(Object dbContext, String id);

    User findById(Object dbContext, String id);

    User findByUsername(Object dbContext, String username);

    void delete(Object dbContext, User data);

    boolean authenticate(Object dbContext, String username, String password);


}
