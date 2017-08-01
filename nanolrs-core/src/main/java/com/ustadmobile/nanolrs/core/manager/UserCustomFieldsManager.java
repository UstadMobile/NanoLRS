package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 7/26/2017.
 */

import com.ustadmobile.nanolrs.core.manager.NanoLrsManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface UserCustomFieldsManager extends NanoLrsManagerSyncable {

    public void createUserCustom(Map<Integer, String> map, User user, Object dbContext) throws SQLException;
    public List<UserCustomFields> findByUser(User user, Object dbContext) throws SQLException;
    public String getUserField(User user, int field, Object dbContext) throws SQLException;
}
