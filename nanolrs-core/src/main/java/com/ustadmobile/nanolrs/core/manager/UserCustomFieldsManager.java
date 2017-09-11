package com.ustadmobile.nanolrs.core.manager;
/**
 * Created by varuna on 7/26/2017.
 */

import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface UserCustomFieldsManager extends NanoLrsManagerSyncable {

    /**
     * Creates new user custom fields for given field map
     * @param map   Custom field map
     * @param user  for this user
     * @param dbContext database context
     * @throws SQLException cuz we doing SQL stuff
     */
    public void createUserCustom(Map<Integer, String> map, User user, Object dbContext) throws SQLException;

    /**
     * Lists all custom fields for specified user
     * @param user  for this user
     * @param dbContext and this database context
     * @return  List of custom fields
     * @throws SQLException
     */
    public List<UserCustomFields> findByUser(User user, Object dbContext) throws SQLException;

    /**
     * Gets custom field for a given user and fieldID
     * @param user  For this user
     * @param field for this field
     * @param dbContext and this database context
     * @return  value in String of the field itself
     * @throws SQLException cuz we're doing SQL stuff
     */
    public String getUserField(User user, int field, Object dbContext) throws SQLException;

    /**
     * Updates / Creates new custom fields for specified user
     * @param map   Map of new custom fields
     * @param user  for this user
     * @param dbContext and this database context
     * @throws SQLException can throw this cause were doing SQL stuff
     */
    public void updateUserCustom(Map<Integer, String> map, User user, Object dbContext) throws SQLException;
}
